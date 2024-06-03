const express = require('express');
const router = express.Router();
const Chat = require('../models/Chat'); // Import your Chat model

router.get('/chat/history/:chatId', async (req, res) => {
    try {
        const chatId = req.params.chatId;
        const chat = await Chat.findById(chatId);
        const messages = chat.messages || [];
        res.status(200).json({ chatId, messages });
    } catch (error) {
        console.error('Error fetching chat history:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

module.exports = router;
