var express = require('express');
var router = express.Router();

let methods = require('../controllers/methods')
/* GET home page. */
router.get('/', methods.get_page);
router.get('/repos',methods.get_page);
router.get('/about',methods.get_page);
router.get('/mostRecentRepos.data',methods.get_recentReposData);
router.get('/reposInfo.data',methods.get_reposInfoData);
router.get('/repos/*',methods.get_reposTemplatePage);
router.get('/repodata/:RepoName/:DataName',methods.get_repoInfoData)

module.exports = router;
