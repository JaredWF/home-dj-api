var resultList;

window.onload = function () {
  document.getElementById("search_button").onclick = function() {
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
                var index = e.target.parentNode.parentNode.sectionRowIndex;
                var uri = resultList[index].uri;
                $.ajax({  
                  type: "POST",  
                  url: "http://home-dj.herokuapp.com/add",  
                  data: JSON.stringify(
                      {id: uri}
                  ),
                  contentType: "application/json",
                  dataType   : "text/plain",
                  success: function(response){  
                    alert("successful post");
                  },
                  error: function(xhr, status, error){  
                    alert(status);
                    alert(xhr.responseText);
                  }
                });
              });
            }
          },
          error: function(e){  
            //alert("failure: \n" + e);  
          }
        });
  };
};