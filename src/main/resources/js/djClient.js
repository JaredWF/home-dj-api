var resultList;
var domain;
var hash;
var tabNumber = 0;

window.onload = function () {
  var url = window.location.href;
  var domainOffset = url.lastIndexOf("/");
  domain = url.slice(0, domainOffset);
  hash = url.slice(domainOffset + 1)


  document.getElementById('search_text').onkeypress = function(e){
    if (!e) e = window.event;
    var keyCode = e.keyCode || e.which;
    if (keyCode == '13' && tabNumber == 1){
      search();
      return false;
    }
  }

  document.getElementById("search_button").onclick = function() {
    if (tabNumber == 1) {
      search();
    }
  }

  showPlaylist();
};

function tabClick(tab) {
  if (tab != tabNumber) {
    document.getElementById("results").innerHTML = "";


    tabNumber = tab;
    var tabs = document.getElementsByClassName("tab");
    var i;
    for (i = 0; i < tabs.length; i++) {
      if (i == tabNumber) {
        tabs[i].id = "selected_tab";
      } else {
        tabs[i].id = "";
      }
    }
  }

  if (tabNumber == 0) {
    showPlaylist();
  }
}

function showPlaylist() {
  $.ajax({  
    type: "GET",  
    url: domain + "/" + hash + "/getAllSongs", 
    dataType: "json",
    success: function(json){ 
      resultList = json;
      var table = document.getElementById("results");
      table.innerHTML = "";

      for (i = 0; i < resultList.length; i++) { 
        var tr = table.appendChild(document.createElement("tr"));

        var albumColumn = tr.appendChild(document.createElement("td"));
        var albumImage = albumColumn.appendChild(document.createElement("img"));
        albumImage.className = "album_image";
        albumImage.src = resultList[i].imageURL;

        var textColumn = tr.appendChild(document.createElement("td"));
        textColumn.className = "song_text"
        var titleDiv = textColumn.appendChild(document.createElement("div"));
        titleDiv.className = "song_title";
        titleDiv.innerHTML = resultList[i].name;
        var artistDiv = textColumn.appendChild(document.createElement("div"));
        artistDiv.className = "artist_name";
        artistDiv.innerHTML = resultList[i].artist;
      }
    },
    error: function(xhr, status, error){  
      console.log(status);
      console.log(xhr.responseText);
      console.log(error);
    }
  });
}

function search() {
  document.activeElement.blur();
  var query = document.getElementById("search_text").value;

  $.ajax({  
      type: "GET",  
      dataType: "json",
      url: "https://api.spotify.com/v1/search?type=track&q=" + query.replace(" ", "%20"),  
      success: function(json){  
        resultList = json.tracks.items;
        var table = document.getElementById("results");
        table.innerHTML = "";

        for (i = 0; i < resultList.length; i++) { 
          var tr = table.appendChild(document.createElement("tr"));

          var albumColumn = tr.appendChild(document.createElement("td"));
          var albumImage = albumColumn.appendChild(document.createElement("img"));
          albumImage.className = "album_image";
          albumImage.src = resultList[i].album.images[2].url;

          var textColumn = tr.appendChild(document.createElement("td"));
          textColumn.className = "song_text"
          var titleDiv = textColumn.appendChild(document.createElement("div"));
          titleDiv.className = "song_title";
          titleDiv.innerHTML = resultList[i].name;
          var artistDiv = textColumn.appendChild(document.createElement("div"));
          artistDiv.className = "artist_name";
          artistDiv.innerHTML = resultList[i].artists[0].name;

          var addColumn = tr.appendChild(document.createElement("td"));
          var addButton = addColumn.appendChild(document.createElement("img"));
          addButton.src = "./AddButton.png";
          addButton.className = "add_button";

          addButton.addEventListener('click', function (e) {
            this.removeEventListener('click', arguments.callee, false);
            this.src = "./CheckButton.png";
            var index = e.target.parentNode.parentNode.sectionRowIndex;
            var uri = resultList[index].uri;
            $.ajax({  
              type: "POST",  
              url: domain + "/" + hash + "/add",  
              data: JSON.stringify(
                  {id: uri}
              ),
              contentType: "application/json",
              dataType   : "text",
              success: function(response){  
                //alert("successful post");
              },
              error: function(xhr, status, error){  
                alert(status);
                alert(xhr.responseText);
                alert(error);
              }
            });
          });
        }
      },
      error: function(e){  
        //alert("failure: \n" + e);  
      }
  });
}