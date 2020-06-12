window.registerExtension('openedge/license_recap', function (options) {
  var isDisplayed = true;
  window.SonarRequest.getJSON('/api/openedge/licenses', { }).then(function (response) {
    if (isDisplayed) {
      options.el.className = 'page page-limited';

      var header = document.createElement('h1');
      header.textContent = 'CABL rules • Licenses';
      options.el.appendChild(header);

      var tbl = document.createElement('table');
      tbl.className = 'data zebra zebra-hover';
      tbl.style.cssText = 'table-layout: fixed;';
      options.el.appendChild(tbl);

      var thead = document.createElement('thead');
      tbl.appendChild(thead);
      var theadLine = document.createElement('tr');
      thead.appendChild(theadLine);

      var theadCol = document.createElement('th');
      theadCol.textContent = 'Company name';
      theadLine.appendChild(theadCol);

      theadCol = document.createElement('th');
      theadCol.textContent = 'Server ID';
      theadCol.width = 200
      theadLine.appendChild(theadCol);

      theadCol = document.createElement('th');
      theadCol.textContent = 'Product';
      theadCol.width = 100
      theadLine.appendChild(theadCol);

      theadCol = document.createElement('th');
      theadCol.textContent = 'Type';
      theadCol.width = 100
      theadLine.appendChild(theadCol);

      theadCol = document.createElement('th');
      theadCol.textContent = 'Repository';
      theadCol.width = 130
      theadLine.appendChild(theadCol);

      theadCol = document.createElement('th');
      theadCol.textContent = 'Expiration';
      theadCol.width = 200
      theadLine.appendChild(theadCol);

      var bdy = document.createElement('tbody');
      tbl.appendChild(bdy);

      for (var i = 0; i < response.licenses.length ; i++) {
        var ln = document.createElement('tr');
        bdy.appendChild(ln);

        var fld1 = document.createElement('td');
        fld1.textContent = response.licenses[i].customer;
        ln.appendChild(fld1);

        var fld2 = document.createElement('td');
        fld2.textContent = response.licenses[i].permanentId;
        ln.appendChild(fld2);

        var fld3 = document.createElement('td');
        fld3.textContent = response.licenses[i].product;
        ln.appendChild(fld3);

        var fld4 = document.createElement('td');
        fld4.textContent = response.licenses[i].type;
        ln.appendChild(fld4);

        var fld5 = document.createElement('td');
        fld5.textContent = response.licenses[i].repository;
        ln.appendChild(fld5);

        var fld6 = document.createElement('td');
        fld6.textContent = response.licenses[i].expiration;
        ln.appendChild(fld6);
      }

      var hr1 = document.createElement('hr');
      hr1.style.cssText = 'height: 8px; margin-top: 10px; margin-bottom: 10px;';
      options.el.appendChild(hr1);
      var header2 = document.createElement('h1');
      header2.textContent = 'CABL rules • Request new license';
      options.el.appendChild(header2);

      var tbl2 = document.createElement('table');
      options.el.appendChild(tbl2);
      var bdy2 = document.createElement('tbody');
      tbl2.appendChild(bdy2);

      var ln4 = document.createElement('tr');
      bdy2.appendChild(ln4);
      var col3 = document.createElement('td');
      col3.style.cssText = 'padding: 10px;';
      ln4.appendChild(col3);

      var ln2 = document.createElement('tr');
      bdy2.appendChild(ln2);
      var col1 = document.createElement('td');
      col1.style.cssText = 'padding: 10px;';
      col1.innerHTML = 'Your CABL licenses can be managed at <a href="https://cabl.riverside-software.fr">https://cabl.riverside-software.fr</a>. ';
      ln2.appendChild(col1);
      var ln3 = document.createElement('tr');
      bdy2.appendChild(ln3);
      var col2 = document.createElement('td');
      col2.style.cssText = 'padding: 10px;';
      ln3.appendChild(col2);
      var pre2 = document.createElement('pre');
      pre2.id = 'cabl-input';
      pre2.style.cssText = 'width: 600px; overflow: auto; background-color: #bbbbbb; border: 1px solid #898989; padding: 5px; white-space: pre-wrap; ';
      col2.appendChild(pre2);
      var code2 = document.createElement('code');
      pre2.appendChild(code2);

      window.SonarRequest.getJSON('/api/riverside/oeinfo', { }).then(function (response) {
         code2.textContent = JSON.stringify(response);

         var cablLink = document.createElement('a');
         cablLink.href = 'https://cabl.riverside-software.fr/#/licenses?licenseRequest=' + encodeURI(JSON.stringify(response));
         cablLink.textContent = 'Acquire or renew license for this server';
         cablLink.rel = 'noopener noreferrer';
         cablLink.target = '_blank';
         col3.appendChild(cablLink);
      }).catch(function (error) {
         code2.textContent = "Riverside Rules plugin is not installed...";
      });
    }
  });

  return function () {
    isDisplayed = false;
  };
});
