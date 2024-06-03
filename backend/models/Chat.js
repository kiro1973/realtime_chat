const mongoose = require('mongoose');
const { messageSchema, Message } = require('./Message');
const ChatSchema = new mongoose.Schema({
    users: [{
      userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
      },
      isChatFavorite: Boolean,
      isThereUnReadMessage: Boolean,
    }],
    messages: [messageSchema], // Using the messageSchema directly
    
  });

module.exports = mongoose.model('Chat', ChatSchema);
