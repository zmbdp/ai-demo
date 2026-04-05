//let baseUrl = 'http://localhost:3000';
let baseUrl = '';
const USER_ID_STORAGE_KEY = 'chat_test_user_id';
$(document).ready(function() {
    const $loginScreen = $('#loginScreen');
    const $chatApp = $('#chatApp');
    const $mockUserId = $('#mockUserId');
    const $mockLoginBtn = $('#mockLoginBtn');
    const $presetUserBtn = $('.preset-user-btn');
    const $switchUserBtn = $('#switchUserBtn');
    const $currentUserIdText = $('#currentUserIdText');
    const $chatHeaderUser = $('#chatHeaderUser');
    const $userInput = $('#userInput');
    const $chatMessages = $('#chatMessages');
    const $sendBtn = $('.send-btn');
    const $historyList = $('.history-list');
    const $deleteSession = $('.delete-session');
    const $chatContainer = $('.chat-container');
    const $actionButtons = $('.action-buttons');
    const $bottomLine = $('.bottom-line');
    let currentUserId = localStorage.getItem(USER_ID_STORAGE_KEY) || '';
    let currentChatId = null; // 当前会话ID
    let replyingSessionId = [];
    let contentWidth = 0;
    // 生成会话ID
    function generateChatId() {
        return 'chat-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
    }

    function requireUserId() {
        if (currentUserId) {
            return true;
        }
        showLoginScreen();
        return false;
    }

    function getQueryString(params) {
        return new URLSearchParams(params).toString();
    }

    function updateUserDisplay() {
        const text = currentUserId || '未设置';
        $currentUserIdText.text(text);
        $chatHeaderUser.text(`当前 userId：${text}`);
    }

    function showLoginScreen() {
        $loginScreen.removeClass('hidden');
        $chatApp.addClass('hidden');
        $mockUserId.val(currentUserId);
        setTimeout(() => $mockUserId.trigger('focus'), 0);
    }

    function showChatApp() {
        $loginScreen.addClass('hidden');
        $chatApp.removeClass('hidden');
        updateUserDisplay();
        loadChatHistory();
        setTimeout(() => $userInput.trigger('focus'), 0);
    }

    function loginWithUserId(userId) {
        currentUserId = String(userId).trim();
        if (!currentUserId) {
            alert('请输入 userId');
            return;
        }
        localStorage.setItem(USER_ID_STORAGE_KEY, currentUserId);
        currentChatId = null;
        replyingSessionId = [];
        $chatMessages.empty();
        $historyList.empty();
        updateUserDisplay();
        showChatApp();
    }

    // 加载历史会话列表
    async function loadChatHistory() {
        if (!requireUserId()) {
            return;
        }
        try {
            const response = await fetch(baseUrl + `/chat/getChatIds?${getQueryString({ userId: currentUserId })}`);
            if (!response.ok) {
                throw new Error('获取历史会话失败');
            }
            const chatIds = await response.json();

            // 清空现有列表
            $historyList.empty();

            // 添加会话到列表
            chatIds.reverse().forEach(chatItem => {
                addChatToHistory(chatItem, false);
            });

            // 高亮当前会话
            updateActiveChat(currentChatId);
        } catch (error) {
            console.error('加载历史会话失败:', error);
        }
    }

    // 加载特定会话的聊天记录
    async function loadChatMessages(chatId) {
        if (!requireUserId()) {
            return;
        }
        try {
            const response = await fetch(baseUrl + `/chat/getChatHistory?${getQueryString({ chatId, userId: currentUserId })}`);
            if (!response.ok) {
                throw new Error('获取会话内容失败');
            }
            const chatData = await response.json();

            // 清空当前聊天区域
            $chatMessages.empty();

            // 显示历史消息
            chatData.forEach(message => {
                const isUser = message.role === 'user';
                if (message.thinking) {
                    // 如果存在思考过程，先显示思考过程
                    const container = createMessageContainer(false);
                    addMessage(message.thinking, false, true, container);
                }
                addMessage(message.content, isUser, false);
            });

            // 滚动到底部
            $chatMessages.scrollTop($chatMessages[0].scrollHeight);
        } catch (error) {
            console.error('加载会话内容失败:', error);
            $chatMessages.empty();
            addMessage('加载历史会话内容失败，请重试', false);
        }
    }

    // 添加会话到历史列表
    function addChatToHistory(chatItem, isNew = true) {
        const $historyItem = $('<div>')
            .addClass('history-item').attr('id', chatItem.chatId)
            .click(function() {
                switchChat(chatItem.chatId);
            });
        let itemText = $('<span>').addClass('itemText ellipse').text(chatItem.title);
        let operatesIcon = $('<span>').addClass('sessionOperates').text('···')
        operatesIcon.click(function(e) {
            e.stopPropagation()
            let rect = e.target.getBoundingClientRect()
            $deleteSession[0].setAttribute('style',
                `--leftP:${rect.left}px; --topP:${rect.top}px; display: block`)
            $deleteSession[0].setAttribute('data-chat-id',  chatItem.chatId)
        });
        $historyItem.append(itemText);
        $historyItem.append(operatesIcon);
        if (isNew) {
            $historyList.prepend($historyItem);
        } else {
            $historyList.append($historyItem);
        }
        updateActiveChat(chatItem.chatId);
    }
    // 删除会话
    function deleteChatFun(chatId) {
        return new Promise((resolve, reject) => {
            fetch(baseUrl + `/chat/deleteChat?${getQueryString({ chatId, userId: currentUserId })}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('删除会话失败');
                }
                resolve();
            })
            .catch(error => {
                console.error('删除会话失败:', error);
                reject();
            });
        })
    }
    // 更新当前激活的会话
    function updateActiveChat(chatId) {
        $('.history-item').removeClass('active');
        $(`.history-item#${chatId}`).addClass('active')
    }

    // 切换到选中的会话
    async function switchChat(chatId) {
        currentChatId = chatId;
        updateActiveChat(chatId);
        await loadChatMessages(chatId);
        $userInput.val('').focus();
    }

    // 新建会话
    function createNewChat(isSend, message = '') {
        currentChatId = generateChatId();
        addChatToHistory({
            chatId: currentChatId,
            title: message || currentChatId
        }, true);
        if (isSend !== 'isSend') {
            $chatMessages.empty();
            $userInput.val('').focus();
            adjustTextareaHeight();
        }
    }

    // 绑定新建会话按钮事件
    $('#newChatBtn').on('click', function() {
        if (!requireUserId()) {
            return;
        }
        createNewChat();
    });

    // 绑定快捷键 Ctrl+K
    $(document).on('keydown', function(e) {
        if (e.ctrlKey && e.key === 'k') {
            e.preventDefault();
            if (!requireUserId()) {
                return;
            }
            createNewChat();
        }
    });

    // 初始化显示登录或聊天页
    // 自动调整文本区域高度
    function adjustTextareaHeight() {
        $userInput.css('height', 'auto');
        $userInput.css('height', $userInput[0].scrollHeight + 'px');
    }

    // 创建消息容器
    function createMessageContainer(isUser = true) {
        const container = $('<div>').addClass('message-container');
        if (!isUser) {
            container.addClass('bot-container');
        }
        $chatMessages.append(container);
        return container;
    }

    // 添加消息到聊天区域
    const throttleScroll = throttle(() => {
        $bottomLine[0].scrollIntoView({
            behavior: 'smooth',
            block: 'end',
            inline: 'end'
        })
    }, 500)
    function addMessage(message, isUser = true, isThinking = false, container = null) {
        const messageDiv = $('<div>').addClass('message').addClass(isUser ? 'user-message' : 'bot-message');
        if (isUser || isThinking) {
            const avatar = $('<div>').addClass('avatar');
            avatar.text(isUser ? '👤' : '🤖');
            messageDiv.append(avatar);
        }
        if (isThinking) {
            messageDiv.addClass('thinking-message');

            // 创建折叠按钮
            const toggleBtn = $('<div>').addClass('toggle-thinking').text('▼');
            messageDiv.addClass('collapsed');

            // 添加点击事件
            toggleBtn.on('click', function() {
                messageDiv.toggleClass('collapsed');
                toggleBtn.text(messageDiv.hasClass('collapsed') ? '▼' : '▲');
            });

            // 创建标题
            const title = $('<div>').addClass('thinking-title').text('已完成推理');
            const titleContainer = $('<div>').addClass('thinking-header')
                .append(toggleBtn)
                .append(title);
            messageDiv.append(titleContainer);
        }

        const content = $('<div>').addClass('content').html(message);
        console.log(content[0], content)
        // 渲染LaTeX公式
        Promise.resolve().then(() => {
            MathJax.typesetPromise(content)
        });

        messageDiv.append(content);

        if (container) {
            container.append(messageDiv);
        } else {
            $chatMessages.append(messageDiv);
        }
        contentWidth = $('.bot-message')[0]?.offsetWidth - 57;
        document.body.setAttribute('style', `--contentWidth:${contentWidth}px`);
        requestAnimationFrame(() => {
            throttleScroll()
        })
        return content;
    }

    // 解析响应内容，提取思考过程和回答
    function parseResponse(text) {
        const thinkMatch = text.match(/<think>([\s\S]*?)<\/think>/);
        let thinking = '';
        let response = text;

        if (thinkMatch) {
            thinking = thinkMatch[1].trim();
            response = text.replace(/<think>[\s\S]*?<\/think>/, '').trim();
        }

        return { thinking, response };
    }

    // 处理发送消息
    async function handleSend() {
        if (!requireUserId()) {
            return;
        }
        const message = $userInput.val().trim();
        if (!message) return;

        // 禁用输入和发送按钮
        $userInput.prop('disabled', true);
        $sendBtn.prop('disabled', true);

        // 添加用户消息
        addMessage(message, true);

        // 创建机器人的回应容器
        let botContainer = createMessageContainer(false);
        let fullResponse = '';
        let thinkingContent = null;
        let responseContent = null;
        let lastThinking = '';
        let lastResponse = '';

        try {
            if (!currentChatId) {
                createNewChat('isSend', message);
            } else {
                // 找到当前会话ID的容器，把标题换成输入的消息
                $(`.history-item#${currentChatId}`).find('.itemText').text(message);
            }
            replyingSessionId.push(currentChatId);
            const response = await fetch(baseUrl + `/chat/stream?${getQueryString({ prompt: message, chatId: currentChatId, userId: currentUserId })}`, {
                method: 'GET',
                headers: {
                    'Accept': 'text/html'
                }
            });
            const reader = response.body.getReader();
            const decoder = new TextDecoder();
            while (true) {
                if (replyingSessionId.includes(currentChatId)) {
                    // 如果当前会话ID不在回复列表中，终止读取
                    let botC = $('.bot-container');
                    if (!botC[0]) {
                        botContainer = createMessageContainer(false);
                    }
                }
                const {done, value} = await reader.read();
                if (done) {
                    replyingSessionId.splice(replyingSessionId.indexOf(currentChatId), 1);
                    break;
                }

                const text = decoder.decode(value);
                fullResponse += text;

                // 解析并更新显示
                const { thinking, response } = parseResponse(fullResponse);

                // 更新思考内容
                if (thinking && thinking !== lastThinking) {
                    lastThinking = thinking;
                    if (!thinkingContent) {
                        thinkingContent = addMessage(thinking, false, true, botContainer);
                    } else {
                        thinkingContent.html(thinking);
                    }
                }

                // 更新回应内容
                if (response && response !== lastResponse) {
                    lastResponse = response;
                    if (responseContent) {
                        responseContent.parent().remove();
                    }
                    responseContent = addMessage(response, false, false, botContainer);
                }

                $chatMessages.scrollTop($chatMessages[0].scrollHeight);
            }
        } catch (error) {
            console.error('Error:', error);
            if (!responseContent) {
                responseContent = addMessage('抱歉，发生错误，请稍后重试。', false, false, botContainer);
            }
        } finally {
            // 重新启用输入和发送按钮
            $userInput.prop('disabled', false);
            $sendBtn.prop('disabled', false);
            $userInput.val('');
            $actionButtons[0].classList.remove('btn-is-active');
            adjustTextareaHeight();
            $userInput.focus();
        }
    }
    function throttle(func, delay) {
        let lastExec = 0;
        let timer = null;

        return function(...args) {
            const now = Date.now();
            const elapsed = now - lastExec;

            // 清除计划中的执行
            if (timer) {
                clearTimeout(timer);
                timer = null;
            }

            // 如果超过延迟时间，立即执行
            if (elapsed >= delay) {
                func.apply(this, args);
                lastExec = now;
            }
            // 否则安排在剩余时间后执行
            else {
                timer = setTimeout(() => {
                    func.apply(this, args);
                    lastExec = Date.now();
                    timer = null;
                }, delay - elapsed);
            }
        };
    }

    // 事件监听器
    function submitLogin() {
        const userId = $mockUserId.val().trim();
        if (!userId) {
            $mockUserId.trigger('focus');
            return;
        }
        loginWithUserId(userId);
    }

    $mockLoginBtn.on('click', submitLogin);

    $mockUserId.on('keydown', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            submitLogin();
        }
    });

    $presetUserBtn.on('click', function() {
        const userId = $(this).data('user-id');
        $mockUserId.val(userId);
        submitLogin();
    });

    $switchUserBtn.on('click', function() {
        showLoginScreen();
    });

    $userInput.on('input', function (e) {
        adjustTextareaHeight();
        if (!e.target.value) {
            $actionButtons[0].classList.remove('btn-is-active');
        } else {
            $actionButtons[0].classList.add('btn-is-active');
        }
    });

    $userInput.on('keydown', function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            handleSend();
        }
    });

    $sendBtn.on('click', handleSend);

    $chatContainer.on('click', function(e) {
        if ($deleteSession[0].style.display === 'block') {
            $deleteSession[0].style.display = 'none';
        }
    })
    $deleteSession.on('click', function(e) {
        e.stopPropagation()
        let chatId = $deleteSession.attr('data-chat-id')
        deleteChatFun(chatId).then(() => {
            $(`.history-item#${chatId}`).remove();
            if (currentChatId === chatId) {
                currentChatId = null;
                $chatMessages.empty();
            }
        })
        $deleteSession[0].style.display = 'none';
    })

    window.onresize = throttle(function() {
        contentWidth = $('.bot-message')[0]?.offsetWidth - 57;
        document.body.setAttribute('style', `--contentWidth:${contentWidth}px`);
    }, 100);

    updateUserDisplay();
    if (currentUserId) {
        showChatApp();
    } else {
        showLoginScreen();
    }
});
