const mongoose = require('mongoose');

const messageSchema = new mongoose.Schema({
  message: String,
  timestamp: {
    type: Date,
    default: Date.now,
  },
  from: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
  },
});
module.exports = {
    Message: mongoose.model('Message', messageSchema),
    messageSchema: messageSchema,
  };