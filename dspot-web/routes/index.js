var express = require('express');
var router = express.Router();

let methods = require('../controllers/methods')
/* GET home page. */
router.get('/', methods.get_page);
router.get('/repos',methods.get_page);
router.get('/about',methods.get_page);
router.get('/data/:state',methods.get_ReposData);
router.get('/reposInfo',methods.get_reposInfoData);
router.get('/repo/*',methods.get_reposTemplatePage);
router.get('/repodata/:user/:reponame/:branchname',methods.get_repoInfoData) /*Still using ?*/
router.post('/reposubmit',methods.post_submitRepo)

module.exports = router;
