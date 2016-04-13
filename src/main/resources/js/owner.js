window.onload = function () {
  var url = window.location.href;

  var codeOffset = url.indexOf("code") + 5; //add 5 to get to the actual code, 'code=' is 5 characters
  var code = url.slice(codeOffset);

  var domainOffset = url.lastIndexOf("/");
  var domain = url.slice(0, domainOffset);

  console.log("code = \n" + code);
  console.log("domain = " + domain);

  loginAndShowPlaylists(code, domain);
};

function loginAndShowPlaylists(code, domain) {
  $.ajax({  
      type: "GET",  
      dataType: "text",
      url: domain + "/finishAuthorize?code=" + code,  
      success: function(hash){
        console.log("hash = " + hash);
        var playlists = getPlaylists(hash, domain);
      },
      error: function(e){  
        //alert("failure: \n" + e);  
      }
  });
}

function getPlaylists(hash, domain) {
  $.ajax({  
    type: "GET",  
    dataType: "json",
    url: domain + "/getPlaylists/" + hash,  
    success: function(json){
      console.log(json);
      displayPlaylists(json);
    },
    error: function(e){  
      //alert("failure: \n" + e);  
    }
  });
}

function displayPlaylists(json) {
  $(".loader").remove();

  var p = document.createElement('p');
  $(p).html("Choose a playlist")
  .appendTo($("#body"));


  for(var i = 0; i < json.length; i++) {
    var playlist = json[i];

    var d = document.createElement('div');
    $(d).addClass("playlistCard")
    .appendTo($("#body"));

    var thumbnail = document.createElement('img');
    $(thumbnail).addClass("albumImage")
    .attr('src', playlist.imageURL)
    .appendTo(d);

    var title = document.createElement('div');
    $(title).addClass("playlistTitle")
    .html(playlist.name)
    .appendTo(d);

    var songCount = document.createElement('div');
    $(songCount).addClass("playlistLength")
    .html(playlist.trackCount + " Songs")
    .appendTo(d);
  }
}