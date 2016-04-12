window.onload = function () {
  var url = window.location.href;

  var codeOffset = url.indexOf("code") + 5; //add 5 to get to the actual code, 'code=' is 5 characters
  var code = url.slice(codeOffset);

  var domainOffset = url.lastIndexOf("/");
  var domain = url.slice(0, domainOffset);

  console.log("code = \n" + code);
  console.log("domain = " + domain);


  $.ajax({  
      type: "GET",  
      dataType: "text",
      url: domain + "/finishAuthorize?code=" + code,  
      success: function(hash){
        console.log("hash = " + hash);

        $.ajax({  
          type: "GET",  
          dataType: "json",
          url: domain + "/getPlaylists/" + hash,  
          success: function(json){
            console.log(json);
          },
          error: function(e){  
            //alert("failure: \n" + e);  
          }
        });
      },
      error: function(e){  
        //alert("failure: \n" + e);  
      }
  });
};