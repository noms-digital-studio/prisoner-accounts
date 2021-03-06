var express = require('express');
var router = new express.Router();

// core routes
router.use('/', require('./home'));
router.use('/prison', require('./prison'));
router.use('/prisoner', require('./prisoner'));
router.use('/account', require('./account'));

// support routes
//router.use('/autocomplete/addresses',  require('./autocomplete/addresses')(config));

module.exports = router;
