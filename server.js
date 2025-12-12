const express = require('express');
const app = express();
const port = 3000
const bodyParser = require('body-parser');

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(express.static('public'));

app.get('/', (req, res) => {
    res.sendFile(__dirname + '/public/index.html');
});
app.use('/api/users', require('./routes/users'));


app.listen(port, () => {
    console.log(`Server is running on port ${port}`);
});

module.exports = app;