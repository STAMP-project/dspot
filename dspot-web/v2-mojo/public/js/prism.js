/* PrismJS 1.17.1
https://prismjs.com/download.html#themes=prism-okaidia&languages=markup+clike+java+javastacktrace+javadoclike+javadoc&plugins=line-numbers+toolbar+highlight-keywords+normalize-whitespace+show-language+copy-to-clipboard+download-button */
var _self="undefined"!=typeof window?window:"undefined"!=typeof WorkerGlobalScope&&self instanceof WorkerGlobalScope?self:{},Prism=function(u){var c=/\blang(?:uage)?-([\w-]+)\b/i,r=0;var _={manual:u.Prism&&u.Prism.manual,disableWorkerMessageHandler:u.Prism&&u.Prism.disableWorkerMessageHandler,util:{encode:function(e){return e instanceof L?new L(e.type,_.util.encode(e.content),e.alias):Array.isArray(e)?e.map(_.util.encode):e.replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/\u00a0/g," ")},type:function(e){return Object.prototype.toString.call(e).slice(8,-1)},objId:function(e){return e.__id||Object.defineProperty(e,"__id",{value:++r}),e.__id},clone:function n(e,t){var a,r,i=_.util.type(e);switch(t=t||{},i){case"Object":if(r=_.util.objId(e),t[r])return t[r];for(var o in a={},t[r]=a,e)e.hasOwnProperty(o)&&(a[o]=n(e[o],t));return a;case"Array":return r=_.util.objId(e),t[r]?t[r]:(a=[],t[r]=a,e.forEach(function(e,r){a[r]=n(e,t)}),a);default:return e}},currentScript:function(){if("undefined"==typeof document)return null;if("currentScript"in document)return document.currentScript;try{throw new Error}catch(e){var r=(/at [^(\r\n]*\((.*):.+:.+\)$/i.exec(e.stack)||[])[1];if(r){var n=document.getElementsByTagName("script");for(var t in n)if(n[t].src==r)return n[t]}return null}}},languages:{extend:function(e,r){var n=_.util.clone(_.languages[e]);for(var t in r)n[t]=r[t];return n},insertBefore:function(n,e,r,t){var a=(t=t||_.languages)[n],i={};for(var o in a)if(a.hasOwnProperty(o)){if(o==e)for(var l in r)r.hasOwnProperty(l)&&(i[l]=r[l]);r.hasOwnProperty(o)||(i[o]=a[o])}var s=t[n];return t[n]=i,_.languages.DFS(_.languages,function(e,r){r===s&&e!=n&&(this[e]=i)}),i},DFS:function e(r,n,t,a){a=a||{};var i=_.util.objId;for(var o in r)if(r.hasOwnProperty(o)){n.call(r,o,r[o],t||o);var l=r[o],s=_.util.type(l);"Object"!==s||a[i(l)]?"Array"!==s||a[i(l)]||(a[i(l)]=!0,e(l,n,o,a)):(a[i(l)]=!0,e(l,n,null,a))}}},plugins:{},highlightAll:function(e,r){_.highlightAllUnder(document,e,r)},highlightAllUnder:function(e,r,n){var t={callback:n,selector:'code[class*="language-"], [class*="language-"] code, code[class*="lang-"], [class*="lang-"] code'};_.hooks.run("before-highlightall",t);for(var a,i=e.querySelectorAll(t.selector),o=0;a=i[o++];)_.highlightElement(a,!0===r,t.callback)},highlightElement:function(e,r,n){var t=function(e){for(;e&&!c.test(e.className);)e=e.parentNode;return e?(e.className.match(c)||[,"none"])[1].toLowerCase():"none"}(e),a=_.languages[t];e.className=e.className.replace(c,"").replace(/\s+/g," ")+" language-"+t;var i=e.parentNode;i&&"pre"===i.nodeName.toLowerCase()&&(i.className=i.className.replace(c,"").replace(/\s+/g," ")+" language-"+t);var o={element:e,language:t,grammar:a,code:e.textContent};function l(e){o.highlightedCode=e,_.hooks.run("before-insert",o),o.element.innerHTML=o.highlightedCode,_.hooks.run("after-highlight",o),_.hooks.run("complete",o),n&&n.call(o.element)}if(_.hooks.run("before-sanity-check",o),!o.code)return _.hooks.run("complete",o),void(n&&n.call(o.element));if(_.hooks.run("before-highlight",o),o.grammar)if(r&&u.Worker){var s=new Worker(_.filename);s.onmessage=function(e){l(e.data)},s.postMessage(JSON.stringify({language:o.language,code:o.code,immediateClose:!0}))}else l(_.highlight(o.code,o.grammar,o.language));else l(_.util.encode(o.code))},highlight:function(e,r,n){var t={code:e,grammar:r,language:n};return _.hooks.run("before-tokenize",t),t.tokens=_.tokenize(t.code,t.grammar),_.hooks.run("after-tokenize",t),L.stringify(_.util.encode(t.tokens),t.language)},matchGrammar:function(e,r,n,t,a,i,o){for(var l in n)if(n.hasOwnProperty(l)&&n[l]){var s=n[l];s=Array.isArray(s)?s:[s];for(var u=0;u<s.length;++u){if(o&&o==l+","+u)return;var c=s[u],g=c.inside,f=!!c.lookbehind,d=!!c.greedy,h=0,m=c.alias;if(d&&!c.pattern.global){var p=c.pattern.toString().match(/[imsuy]*$/)[0];c.pattern=RegExp(c.pattern.source,p+"g")}c=c.pattern||c;for(var y=t,v=a;y<r.length;v+=r[y].length,++y){var k=r[y];if(r.length>e.length)return;if(!(k instanceof L)){if(d&&y!=r.length-1){if(c.lastIndex=v,!(O=c.exec(e)))break;for(var b=O.index+(f&&O[1]?O[1].length:0),w=O.index+O[0].length,A=y,P=v,x=r.length;A<x&&(P<w||!r[A].type&&!r[A-1].greedy);++A)(P+=r[A].length)<=b&&(++y,v=P);if(r[y]instanceof L)continue;S=A-y,k=e.slice(v,P),O.index-=v}else{c.lastIndex=0;var O=c.exec(k),S=1}if(O){f&&(h=O[1]?O[1].length:0);w=(b=O.index+h)+(O=O[0].slice(h)).length;var j=k.slice(0,b),N=k.slice(w),E=[y,S];j&&(++y,v+=j.length,E.push(j));var C=new L(l,g?_.tokenize(O,g):O,m,O,d);if(E.push(C),N&&E.push(N),Array.prototype.splice.apply(r,E),1!=S&&_.matchGrammar(e,r,n,y,v,!0,l+","+u),i)break}else if(i)break}}}}},tokenize:function(e,r){var n=[e],t=r.rest;if(t){for(var a in t)r[a]=t[a];delete r.rest}return _.matchGrammar(e,n,r,0,0,!1),n},hooks:{all:{},add:function(e,r){var n=_.hooks.all;n[e]=n[e]||[],n[e].push(r)},run:function(e,r){var n=_.hooks.all[e];if(n&&n.length)for(var t,a=0;t=n[a++];)t(r)}},Token:L};function L(e,r,n,t,a){this.type=e,this.content=r,this.alias=n,this.length=0|(t||"").length,this.greedy=!!a}if(u.Prism=_,L.stringify=function(e,r){if("string"==typeof e)return e;if(Array.isArray(e))return e.map(function(e){return L.stringify(e,r)}).join("");var n={type:e.type,content:L.stringify(e.content,r),tag:"span",classes:["token",e.type],attributes:{},language:r};if(e.alias){var t=Array.isArray(e.alias)?e.alias:[e.alias];Array.prototype.push.apply(n.classes,t)}_.hooks.run("wrap",n);var a=Object.keys(n.attributes).map(function(e){return e+'="'+(n.attributes[e]||"").replace(/"/g,"&quot;")+'"'}).join(" ");return"<"+n.tag+' class="'+n.classes.join(" ")+'"'+(a?" "+a:"")+">"+n.content+"</"+n.tag+">"},!u.document)return u.addEventListener&&(_.disableWorkerMessageHandler||u.addEventListener("message",function(e){var r=JSON.parse(e.data),n=r.language,t=r.code,a=r.immediateClose;u.postMessage(_.highlight(t,_.languages[n],n)),a&&u.close()},!1)),_;var e=_.util.currentScript();if(e&&(_.filename=e.src,e.hasAttribute("data-manual")&&(_.manual=!0)),!_.manual){function n(){_.manual||_.highlightAll()}var t=document.readyState;"loading"===t||"interactive"===t&&e.defer?document.addEventListener("DOMContentLoaded",n):window.requestAnimationFrame?window.requestAnimationFrame(n):window.setTimeout(n,16)}return _}(_self);"undefined"!=typeof module&&module.exports&&(module.exports=Prism),"undefined"!=typeof global&&(global.Prism=Prism);
Prism.languages.markup={comment:/<!--[\s\S]*?-->/,prolog:/<\?[\s\S]+?\?>/,doctype:{pattern:/<!DOCTYPE(?:[^>"'[\]]|"[^"]*"|'[^']*')+(?:\[(?:(?!<!--)[^"'\]]|"[^"]*"|'[^']*'|<!--[\s\S]*?-->)*\]\s*)?>/i,greedy:!0},cdata:/<!\[CDATA\[[\s\S]*?]]>/i,tag:{pattern:/<\/?(?!\d)[^\s>\/=$<%]+(?:\s(?:\s*[^\s>\/=]+(?:\s*=\s*(?:"[^"]*"|'[^']*'|[^\s'">=]+(?=[\s>]))|(?=[\s/>])))+)?\s*\/?>/i,greedy:!0,inside:{tag:{pattern:/^<\/?[^\s>\/]+/i,inside:{punctuation:/^<\/?/,namespace:/^[^\s>\/:]+:/}},"attr-value":{pattern:/=\s*(?:"[^"]*"|'[^']*'|[^\s'">=]+)/i,inside:{punctuation:[/^=/,{pattern:/^(\s*)["']|["']$/,lookbehind:!0}]}},punctuation:/\/?>/,"attr-name":{pattern:/[^\s>\/]+/,inside:{namespace:/^[^\s>\/:]+:/}}}},entity:/&#?[\da-z]{1,8};/i},Prism.languages.markup.tag.inside["attr-value"].inside.entity=Prism.languages.markup.entity,Prism.hooks.add("wrap",function(a){"entity"===a.type&&(a.attributes.title=a.content.replace(/&amp;/,"&"))}),Object.defineProperty(Prism.languages.markup.tag,"addInlined",{value:function(a,e){var s={};s["language-"+e]={pattern:/(^<!\[CDATA\[)[\s\S]+?(?=\]\]>$)/i,lookbehind:!0,inside:Prism.languages[e]},s.cdata=/^<!\[CDATA\[|\]\]>$/i;var n={"included-cdata":{pattern:/<!\[CDATA\[[\s\S]*?\]\]>/i,inside:s}};n["language-"+e]={pattern:/[\s\S]+/,inside:Prism.languages[e]};var t={};t[a]={pattern:RegExp("(<__[\\s\\S]*?>)(?:<!\\[CDATA\\[[\\s\\S]*?\\]\\]>\\s*|[\\s\\S])*?(?=<\\/__>)".replace(/__/g,a),"i"),lookbehind:!0,greedy:!0,inside:n},Prism.languages.insertBefore("markup","cdata",t)}}),Prism.languages.xml=Prism.languages.extend("markup",{}),Prism.languages.html=Prism.languages.markup,Prism.languages.mathml=Prism.languages.markup,Prism.languages.svg=Prism.languages.markup;
Prism.languages.clike={comment:[{pattern:/(^|[^\\])\/\*[\s\S]*?(?:\*\/|$)/,lookbehind:!0},{pattern:/(^|[^\\:])\/\/.*/,lookbehind:!0,greedy:!0}],string:{pattern:/(["'])(?:\\(?:\r\n|[\s\S])|(?!\1)[^\\\r\n])*\1/,greedy:!0},"class-name":{pattern:/(\b(?:class|interface|extends|implements|trait|instanceof|new)\s+|\bcatch\s+\()[\w.\\]+/i,lookbehind:!0,inside:{punctuation:/[.\\]/}},keyword:/\b(?:if|else|while|do|for|return|in|instanceof|function|new|try|throw|catch|finally|null|break|continue)\b/,boolean:/\b(?:true|false)\b/,function:/\w+(?=\()/,number:/\b0x[\da-f]+\b|(?:\b\d+\.?\d*|\B\.\d+)(?:e[+-]?\d+)?/i,operator:/[<>]=?|[!=]=?=?|--?|\+\+?|&&?|\|\|?|[?*/~^%]/,punctuation:/[{}[\];(),.:]/};
!function(e){var t=/\b(?:abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|exports|extends|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|module|native|new|null|open|opens|package|private|protected|provides|public|requires|return|short|static|strictfp|super|switch|synchronized|this|throw|throws|to|transient|transitive|try|uses|var|void|volatile|while|with|yield)\b/,a=/\b[A-Z](?:\w*[a-z]\w*)?\b/;e.languages.java=e.languages.extend("clike",{"class-name":[a,/\b[A-Z]\w*(?=\s+\w+\s*[;,=())])/],keyword:t,function:[e.languages.clike.function,{pattern:/(\:\:)[a-z_]\w*/,lookbehind:!0}],number:/\b0b[01][01_]*L?\b|\b0x[\da-f_]*\.?[\da-f_p+-]+\b|(?:\b\d[\d_]*\.?[\d_]*|\B\.\d[\d_]*)(?:e[+-]?\d[\d_]*)?[dfl]?/i,operator:{pattern:/(^|[^.])(?:<<=?|>>>?=?|->|--|\+\+|&&|\|\||::|[?:~]|[-+*/%&|^!=<>]=?)/m,lookbehind:!0}}),e.languages.insertBefore("java","string",{"triple-quoted-string":{pattern:/"""[ \t]*[\r\n](?:(?:"|"")?(?:\\.|[^"\\]))*"""/,greedy:!0,alias:"string"}}),e.languages.insertBefore("java","class-name",{annotation:{alias:"punctuation",pattern:/(^|[^.])@\w+/,lookbehind:!0},namespace:{pattern:/(\b(?:exports|import(?:\s+static)?|module|open|opens|package|provides|requires|to|transitive|uses|with)\s+)[a-z]\w*(?:\.[a-z]\w*)+/,lookbehind:!0,inside:{punctuation:/\./}},generics:{pattern:/<(?:[\w\s,.&?]|<(?:[\w\s,.&?]|<(?:[\w\s,.&?]|<[\w\s,.&?]*>)*>)*>)*>/,inside:{"class-name":a,keyword:t,punctuation:/[<>(),.:]/,operator:/[?&|]/}}})}(Prism);
Prism.languages.javastacktrace={summary:{pattern:/^[\t ]*(?:(?:Caused by:|Suppressed:|Exception in thread "[^"]*")[\t ]+)?[\w$.]+(?:\:.*)?$/m,inside:{keyword:{pattern:/^(\s*)(?:(?:Caused by|Suppressed)(?=:)|Exception in thread)/m,lookbehind:!0},string:{pattern:/^(\s*)"[^"]*"/,lookbehind:!0},exceptions:{pattern:/^(:?\s*)[\w$.]+(?=:|$)/,lookbehind:!0,inside:{"class-name":/[\w$]+(?=$|:)/,namespace:/[a-z]\w*/,punctuation:/[.:]/}},message:{pattern:/(:\s*)\S.*/,lookbehind:!0,alias:"string"},punctuation:/[:]/}},"stack-frame":{pattern:/^[\t ]*at [\w$.]+(?:<init>)?\([^()]*\)/m,inside:{keyword:{pattern:/^(\s*)at/,lookbehind:!0},source:[{pattern:/(\()\w+.\w+:\d+(?=\))/,lookbehind:!0,inside:{file:/^\w+\.\w+/,punctuation:/:/,"line-number":{pattern:/\d+/,alias:"number"}}},{pattern:/(\()[^()]*(?=\))/,lookbehind:!0,inside:{keyword:/^(?:Unknown Source|Native Method)$/}}],"class-name":/[\w$]+(?=\.(?:<init>|[\w$]+)\()/,function:/(?:<init>|[\w$]+)(?=\()/,namespace:/[a-z]\w*/,punctuation:/[.()]/}},more:{pattern:/^[\t ]*\.{3} \d+ [a-z]+(?: [a-z]+)*/m,inside:{punctuation:/\.{3}/,number:/\d+/,keyword:/\b[a-z]+(?: [a-z]+)*\b/}}};
!function(p){var a=p.languages.javadoclike={parameter:{pattern:/(^\s*(?:\/{3}|\*|\/\*\*)\s*@(?:param|arg|arguments)\s+)\w+/m,lookbehind:!0},keyword:{pattern:/(^\s*(?:\/{3}|\*|\/\*\*)\s*|\{)@[a-z][a-zA-Z-]+\b/m,lookbehind:!0},punctuation:/[{}]/};Object.defineProperty(a,"addSupport",{value:function(a,e){"string"==typeof a&&(a=[a]),a.forEach(function(a){!function(a,e){var n="doc-comment",t=p.languages[a];if(t){var r=t[n];if(!r){var o={"doc-comment":{pattern:/(^|[^\\])\/\*\*[^/][\s\S]*?(?:\*\/|$)/,lookbehind:!0,alias:"comment"}};r=(t=p.languages.insertBefore(a,"comment",o))[n]}if(r instanceof RegExp&&(r=t[n]={pattern:r}),Array.isArray(r))for(var i=0,s=r.length;i<s;i++)r[i]instanceof RegExp&&(r[i]={pattern:r[i]}),e(r[i]);else e(r)}}(a,function(a){a.inside||(a.inside={}),a.inside.rest=e})})}}),a.addSupport(["java","javascript","php"],a)}(Prism);
!function(a){var e={code:{pattern:/(^(?:\s*(?:\*\s*)*)).*[^*\s].+$/m,lookbehind:!0,inside:a.languages.java,alias:"language-java"}};a.languages.javadoc=a.languages.extend("javadoclike",{}),a.languages.insertBefore("javadoc","keyword",{"class-name":[{pattern:/(@(?:exception|throws|see|link|linkplain|value)\s+(?:[a-z\d]+\.)*)[A-Z](?:\w*[a-z]\w*)?(?:\.[A-Z](?:\w*[a-z]\w*)?)*/,lookbehind:!0,inside:{punctuation:/\./}},{pattern:/(@param\s+)<[A-Z]\w*>/,lookbehind:!0,inside:{punctuation:/[.<>]/}}],namespace:{pattern:/(@(?:exception|throws|see|link|linkplain)\s+)(?:[a-z\d]+\.)+/,lookbehind:!0,inside:{punctuation:/\./}},"code-section":[{pattern:/(\{@code\s+)(?:[^{}]|\{[^{}]*\})+?(?=\s*\})/,lookbehind:!0,inside:e},{pattern:/(<(code|tt)>\s*)[\s\S]+?(?=\s*<\/\2>)/,lookbehind:!0,inside:e}],tag:a.languages.markup.tag}),a.languages.javadoclike.addSupport("java",a.languages.javadoc)}(Prism);
!function(){if("undefined"!=typeof self&&self.Prism&&self.document){var l="line-numbers",c=/\n(?!$)/g,m=function(e){var t=a(e)["white-space"];if("pre-wrap"===t||"pre-line"===t){var n=e.querySelector("code"),r=e.querySelector(".line-numbers-rows"),s=e.querySelector(".line-numbers-sizer"),i=n.textContent.split(c);s||((s=document.createElement("span")).className="line-numbers-sizer",n.appendChild(s)),s.style.display="block",i.forEach(function(e,t){s.textContent=e||"\n";var n=s.getBoundingClientRect().height;r.children[t].style.height=n+"px"}),s.textContent="",s.style.display="none"}},a=function(e){return e?window.getComputedStyle?getComputedStyle(e):e.currentStyle||null:null};window.addEventListener("resize",function(){Array.prototype.forEach.call(document.querySelectorAll("pre."+l),m)}),Prism.hooks.add("complete",function(e){if(e.code){var t=e.element,n=t.parentNode;if(n&&/pre/i.test(n.nodeName)&&!t.querySelector(".line-numbers-rows")){for(var r=!1,s=/(?:^|\s)line-numbers(?:\s|$)/,i=t;i;i=i.parentNode)if(s.test(i.className)){r=!0;break}if(r){t.className=t.className.replace(s," "),s.test(n.className)||(n.className+=" line-numbers");var l,a=e.code.match(c),o=a?a.length+1:1,u=new Array(o+1).join("<span></span>");(l=document.createElement("span")).setAttribute("aria-hidden","true"),l.className="line-numbers-rows",l.innerHTML=u,n.hasAttribute("data-start")&&(n.style.counterReset="linenumber "+(parseInt(n.getAttribute("data-start"),10)-1)),e.element.appendChild(l),m(n),Prism.hooks.run("line-numbers",e)}}}}),Prism.hooks.add("line-numbers",function(e){e.plugins=e.plugins||{},e.plugins.lineNumbers=!0}),Prism.plugins.lineNumbers={getLine:function(e,t){if("PRE"===e.tagName&&e.classList.contains(l)){var n=e.querySelector(".line-numbers-rows"),r=parseInt(e.getAttribute("data-start"),10)||1,s=r+(n.children.length-1);t<r&&(t=r),s<t&&(t=s);var i=t-r;return n.children[i]}}}}}();
!function(){if("undefined"!=typeof self&&self.Prism&&self.document){var r=[],i={},a=function(){};Prism.plugins.toolbar={};var t=Prism.plugins.toolbar.registerButton=function(t,a){var e;e="function"==typeof a?a:function(t){var e;return"function"==typeof a.onClick?((e=document.createElement("button")).type="button",e.addEventListener("click",function(){a.onClick.call(this,t)})):"string"==typeof a.url?(e=document.createElement("a")).href=a.url:e=document.createElement("span"),a.className&&e.classList.add(a.className),e.textContent=a.text,e},t in i?console.warn('There is a button with the key "'+t+'" registered already.'):r.push(i[t]=e)},e=Prism.plugins.toolbar.hook=function(n){var t=n.element.parentNode;if(t&&/pre/i.test(t.nodeName)&&!t.parentNode.classList.contains("code-toolbar")){var e=document.createElement("div");e.classList.add("code-toolbar"),t.parentNode.insertBefore(e,t),e.appendChild(t);var o=document.createElement("div");o.classList.add("toolbar"),document.body.hasAttribute("data-toolbar-order")&&(r=document.body.getAttribute("data-toolbar-order").split(",").map(function(t){return i[t]||a})),r.forEach(function(t){var e=t(n);if(e){var a=document.createElement("div");a.classList.add("toolbar-item"),a.appendChild(e),o.appendChild(a)}}),e.appendChild(o)}};t("label",function(t){var e=t.element.parentNode;if(e&&/pre/i.test(e.nodeName)&&e.hasAttribute("data-label")){var a,n,o=e.getAttribute("data-label");try{n=document.querySelector("template#"+o)}catch(t){}return n?a=n.content:(e.hasAttribute("data-url")?(a=document.createElement("a")).href=e.getAttribute("data-url"):a=document.createElement("span"),a.textContent=o),a}}),Prism.hooks.add("complete",e)}}();
"undefined"!=typeof self&&!self.Prism||"undefined"!=typeof global&&!global.Prism||Prism.hooks.add("wrap",function(e){"keyword"===e.type&&e.classes.push("keyword-"+e.content)});
!function(){var i=Object.assign||function(e,n){for(var t in n)n.hasOwnProperty(t)&&(e[t]=n[t]);return e};function e(e){this.defaults=i({},e)}function l(e){for(var n=0,t=0;t<e.length;++t)e.charCodeAt(t)=="\t".charCodeAt(0)&&(n+=3);return e.length+n}e.prototype={setDefaults:function(e){this.defaults=i(this.defaults,e)},normalize:function(e,n){for(var t in n=i(this.defaults,n)){var r=t.replace(/-(\w)/g,function(e,n){return n.toUpperCase()});"normalize"!==t&&"setDefaults"!==r&&n[t]&&this[r]&&(e=this[r].call(this,e,n[t]))}return e},leftTrim:function(e){return e.replace(/^\s+/,"")},rightTrim:function(e){return e.replace(/\s+$/,"")},tabsToSpaces:function(e,n){return n=0|n||4,e.replace(/\t/g,new Array(++n).join(" "))},spacesToTabs:function(e,n){return n=0|n||4,e.replace(RegExp(" {"+n+"}","g"),"\t")},removeTrailing:function(e){return e.replace(/\s*?$/gm,"")},removeInitialLineFeed:function(e){return e.replace(/^(?:\r?\n|\r)/,"")},removeIndent:function(e){var n=e.match(/^[^\S\n\r]*(?=\S)/gm);return n&&n[0].length?(n.sort(function(e,n){return e.length-n.length}),n[0].length?e.replace(RegExp("^"+n[0],"gm"),""):e):e},indent:function(e,n){return e.replace(/^[^\S\n\r]*(?=\S)/gm,new Array(++n).join("\t")+"$&")},breakLines:function(e,n){n=!0===n?80:0|n||80;for(var t=e.split("\n"),r=0;r<t.length;++r)if(!(l(t[r])<=n)){for(var i=t[r].split(/(\s+)/g),o=0,a=0;a<i.length;++a){var s=l(i[a]);n<(o+=s)&&(i[a]="\n"+i[a],o=s)}t[r]=i.join("")}return t.join("\n")}},"undefined"!=typeof module&&module.exports&&(module.exports=e),"undefined"!=typeof Prism&&(Prism.plugins.NormalizeWhitespace=new e({"remove-trailing":!0,"remove-indent":!0,"left-trim":!0,"right-trim":!0}),Prism.hooks.add("before-sanity-check",function(e){var n=Prism.plugins.NormalizeWhitespace;if(!e.settings||!1!==e.settings["whitespace-normalization"])if(e.element&&e.element.parentNode||!e.code){var t=e.element.parentNode,r=/(?:^|\s)no-whitespace-normalization(?:\s|$)/;if(e.code&&t&&"pre"===t.nodeName.toLowerCase()&&!r.test(t.className)&&!r.test(e.element.className)){for(var i=t.childNodes,o="",a="",s=!1,l=0;l<i.length;++l){var c=i[l];c==e.element?s=!0:"#text"===c.nodeName&&(s?a+=c.nodeValue:o+=c.nodeValue,t.removeChild(c),--l)}if(e.element.children.length&&Prism.plugins.KeepMarkup){var u=o+e.element.innerHTML+a;e.element.innerHTML=n.normalize(u,e.settings),e.code=e.element.textContent}else e.code=o+e.code+a,e.code=n.normalize(e.code,e.settings)}}else e.code=n.normalize(e.code,e.settings)}))}();
!function(){if("undefined"!=typeof self&&self.Prism&&self.document)if(Prism.plugins.toolbar){var r={html:"HTML",xml:"XML",svg:"SVG",mathml:"MathML",css:"CSS",clike:"C-like",js:"JavaScript",abap:"ABAP",abnf:"Augmented Backus–Naur form",antlr4:"ANTLR4",g4:"ANTLR4",apacheconf:"Apache Configuration",apl:"APL",aql:"AQL",arff:"ARFF",asciidoc:"AsciiDoc",adoc:"AsciiDoc",asm6502:"6502 Assembly",aspnet:"ASP.NET (C#)",autohotkey:"AutoHotkey",autoit:"AutoIt",shell:"Bash",basic:"BASIC",bbcode:"BBcode",bnf:"Backus–Naur form",rbnf:"Routing Backus–Naur form",csharp:"C#",cs:"C#",dotnet:"C#",cpp:"C++",cil:"CIL",coffee:"CoffeeScript",cmake:"CMake",csp:"Content-Security-Policy","css-extras":"CSS Extras",django:"Django/Jinja2",jinja2:"Django/Jinja2","dns-zone-file":"DNS zone file","dns-zone":"DNS zone file",dockerfile:"Docker",ebnf:"Extended Backus–Naur form",ejs:"EJS",etlua:"Embedded Lua templating",erb:"ERB",fsharp:"F#","firestore-security-rules":"Firestore security rules",ftl:"FreeMarker Template Language",gcode:"G-code",gdscript:"GDScript",gedcom:"GEDCOM",glsl:"GLSL",gml:"GameMaker Language",gamemakerlanguage:"GameMaker Language",graphql:"GraphQL",hs:"Haskell",hcl:"HCL",http:"HTTP",hpkp:"HTTP Public-Key-Pins",hsts:"HTTP Strict-Transport-Security",ichigojam:"IchigoJam",inform7:"Inform 7",javadoc:"JavaDoc",javadoclike:"JavaDoc-like",javastacktrace:"Java stack trace",jq:"JQ",jsdoc:"JSDoc","js-extras":"JS Extras","js-templates":"JS Templates",json:"JSON",jsonp:"JSONP",json5:"JSON5",latex:"LaTeX",tex:"TeX",context:"ConTeXt",lilypond:"LilyPond",ly:"LilyPond",emacs:"Lisp",elisp:"Lisp","emacs-lisp":"Lisp",lolcode:"LOLCODE",md:"Markdown","markup-templating":"Markup templating",matlab:"MATLAB",mel:"MEL",moon:"MoonScript",n1ql:"N1QL",n4js:"N4JS",n4jsd:"N4JS","nand2tetris-hdl":"Nand To Tetris HDL",nasm:"NASM",nginx:"nginx",nsis:"NSIS",objectivec:"Objective-C",ocaml:"OCaml",opencl:"OpenCL",parigp:"PARI/GP",objectpascal:"Object Pascal",pcaxis:"PC-Axis",px:"PC-Axis",php:"PHP",phpdoc:"PHPDoc","php-extras":"PHP Extras",plsql:"PL/SQL",powershell:"PowerShell",properties:".properties",protobuf:"Protocol Buffers",py:"Python",q:"Q (kdb+ database)",jsx:"React JSX",tsx:"React TSX",renpy:"Ren'py",rest:"reST (reStructuredText)","robot-framework":"Robot Framework",robot:"Robot Framework",rb:"Ruby",sas:"SAS",sass:"Sass (Sass)",scss:"Sass (Scss)","shell-session":"Shell session",solidity:"Solidity (Ethereum)",soy:"Soy (Closure Template)",sparql:"SPARQL",rq:"SPARQL","splunk-spl":"Splunk SPL",sqf:"SQF: Status Quo Function (Arma 3)",sql:"SQL",tap:"TAP",toml:"TOML",tt2:"Template Toolkit 2",trig:"TriG",ts:"TypeScript","t4-cs":"T4 Text Templates (C#)",t4:"T4 Text Templates (C#)","t4-vb":"T4 Text Templates (VB)","t4-templating":"T4 templating",vbnet:"VB.Net",vhdl:"VHDL",vim:"vim","visual-basic":"Visual Basic",vb:"Visual Basic",wasm:"WebAssembly",wiki:"Wiki markup",xeoracube:"XeoraCube",xojo:"Xojo (REALbasic)",xquery:"XQuery",yaml:"YAML",yml:"YAML"};Prism.plugins.toolbar.registerButton("show-language",function(e){var a=e.element.parentNode;if(a&&/pre/i.test(a.nodeName)){var s,t=a.getAttribute("data-language")||r[e.language]||((s=e.language)?(s.substring(0,1).toUpperCase()+s.substring(1)).replace(/s(?=cript)/,"S"):s);if(t){var o=document.createElement("span");return o.textContent=t,o}}})}else console.warn("Show Languages plugin loaded before Toolbar plugin.")}();
!function(){if("undefined"!=typeof self&&self.Prism&&self.document)if(Prism.plugins.toolbar){var r=window.ClipboardJS||void 0;r||"function"!=typeof require||(r=require("clipboard"));var i=[];if(!r){var o=document.createElement("script"),e=document.querySelector("head");o.onload=function(){if(r=window.ClipboardJS)for(;i.length;)i.pop()()},o.src="https://cdnjs.cloudflare.com/ajax/libs/clipboard.js/2.0.0/clipboard.min.js",e.appendChild(o)}Prism.plugins.toolbar.registerButton("copy-to-clipboard",function(e){var t=document.createElement("button");return t.textContent="Copy",r?o():i.push(o),t;function o(){var o=new r(t,{text:function(){return e.code}});o.on("success",function(){t.textContent="Copied!",n()}),o.on("error",function(){t.textContent="Press Ctrl+C to copy",n()})}function n(){setTimeout(function(){t.textContent="Copy"},5e3)}})}else console.warn("Copy to Clipboard plugin loaded before Toolbar plugin.")}();
"undefined"!=typeof self&&self.Prism&&self.document&&document.querySelector&&Prism.plugins.toolbar.registerButton("download-file",function(t){var e=t.element.parentNode;if(e&&/pre/i.test(e.nodeName)&&e.hasAttribute("data-src")&&e.hasAttribute("data-download-link")){var a=e.getAttribute("data-src"),n=document.createElement("a");return n.textContent=e.getAttribute("data-download-link-label")||"Download",n.setAttribute("download",""),n.href=a,n}});