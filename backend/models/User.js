const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  gImage: String,
  emailId: String,
  gName: String,
  socketId: String,
});

module.exports = mongoose.model('User', userSchema);
