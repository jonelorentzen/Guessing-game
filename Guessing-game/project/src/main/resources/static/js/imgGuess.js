var currentSegment;

$(document).ready(function () {
$('#hiscorelist').DataTable({
"order": [[ 1, "desc" ]]
});
$('.dataTables_length').addClass('bs-select');
});
	
window.onload = function(){	
	$('#img0').on("mousedown", function(event) {
	  var x = event.pageX - $('#img0').offset().left,
	      y = event.pageY - $('#img0').offset().top,
	    alpha;
		console.log("Left: " + x + " - Top: " + y);
		var i;
		for (i = 0; i < 49; i++) { 
			var ctx = new OffscreenCanvas(this.width, this.height).getContext("2d");
			var img = document.getElementById("img" + i);
			img.crossOrigin = "Anonymous";
			ctx.drawImage( img, 0, 0 );
			alpha = ctx.getImageData(x, y, 1, 1).data[3]; // [0]R [1]G [2]B [3]A
	
			if( alpha!=0 ) {
				console.log("pic" + i);
				if (!$("#img" + i).hasClass("segment-used")) {
					$("#img" + i).addClass("segment-chosen");
					currentSegment = i;
				}
			} else {
				$("#img" + i).removeClass("segment-chosen");
			}
		}
	});
	if ($page == "gamescreen") {
		getGameInfo();
	}
};
	
function sendSegment() {
	$.post( "/sendUpdate", { segment: currentSegment })
  	.done(function( data ) {
  });	
}

function changePicture() {
	window.location.href = "game?selectedlabel=" + $("#dropOperator").val();					
}

function sendGuess() {
	$.post( "/sendUpdate", { guess: $("#txtguess").val() })
  	.done(function( data ) {
  });
}

function sendAIReq() {
	$.get("/startAI", function (data, status, xhr) {

  });
}
function goNewGame() {
	window.location.href = "/";	
}
function goLeaderboard() {
	window.location.href = "/leaderboard";	
}
function sendPictureID() {
	$.post( "/sendUpdate", { pictureId: $("#dropOperator").val() })
  	.done(function( data ) {
    console.log( "Data Loaded: " + data );
  });	
}

function sendLogin() {
	$.post( "/gameLogin", { username: $("#login").val(), password: $("#password").val() })
  	.done(function( data, status ) {
  	Cookies.remove("username");
  	Cookies.set("username", data.username);
	setTimeout(getLobbyStatus,100);	
  });
	
}

function getLobbyStatus() { 
	$.get("/gameUpdate", function (data, status, xhr) {
		if (data.queuing == "true") {
			setTimeout(getLobbyStatus,500);	
			if (!$("#loginForm").hasClass("d-none")) {
				$("#loginForm").addClass("d-none");
			}				
			if ($("#lobbyDiv").hasClass("d-none")) {
				$("#lobbyDiv").removeClass("d-none");
			}
		} else {
			$.get("/gameUpdate", function (data, status, xhr) {
				if (data.playerRole == "GUESSER" && data.gameStatus == "WAITING") {
					setTimeout(getLobbyStatus,500);
					if (!$("#loginForm").hasClass("d-none")) {
						$("#loginForm").addClass("d-none");
					}				
					if (!$("#ai_btn").hasClass("d-none")) {
						$("#ai_btn").addClass("d-none");
					}
					if ($("#lobbydiv").hasClass("d-none")) {
						$("#lobbydiv").removeClass("d-none");
					}
				} else 	if (data.playerRole == "GUESSER" && data.gameStatus == "PLAYING") {
					window.location.href = "game";
				}	else if (data.playerRole == "PROPOSER") {
					window.location.href = "game";					
				}
			}
		)}
	});
}

function getGameInfo() { // Regularly called function to update the game from data requested by gameUpdate
	$.get("/gameUpdate", function (data, status, xhr) {
		if (data.playerRole == "GUESSER") {
			if (!$("#picChooser").hasClass("d-none")) {
				$("#picChooser").addClass("d-none");
			}
			if (!$("#sendSegment").hasClass("d-none")) {
				$("#sendSegment").addClass("d-none");
			}
			if ($("#picGuess").hasClass("d-none")) {
				$("#picGuess").removeClass("d-none");
			}			
			var i;
			for (i = 0; i < 49; i++) { 
				if (jQuery.inArray( i, data.segments) == -1) {
					if (!$("#img" + i).hasClass("d-none")) {
						$("#img" + i).addClass("d-none");
					} 
				} else {
					$("#img" + i).removeClass("d-none");
				}
			}
			if ($("#pictureContainer").hasClass("d-none")) {
				$("#pictureContainer").removeClass("d-none");
			} 
			if (data.playerActive == "WAITING") {
				$('#sendGuess').attr("disabled", true);	
			} else if (data.playerActive == "PLAYING") {
				$('#sendGuess').attr("disabled", false);	
			}
		} else 	if (data.playerRole == "PROPOSER") {
			if (data.gameStatus == "WAITING") {
				if ($("#picChooser").hasClass("d-none")) {
					$("#picChooser").removeClass("d-none");
				}
				if (!$("#sendSegment").hasClass("d-none")) {
					$("#sendSegment").addClass("d-none");
				}
			} else if (data.gameStatus == "PLAYING") {
				if (!$("#picChooser").hasClass("d-none")) {
					$("#picChooser").addClass("d-none");
				}
				if ($("#sendSegment").hasClass("d-none")) {
					$("#sendSegment").removeClass("d-none");	
				}
			} 
			if (data.playerActive == "WAITING") {
				$('#sendSegment').attr("disabled", true);	
			} else if (data.playerActive == "PLAYING") {
				$('#sendSegment').attr("disabled", false);	
			}
	
		if (!$("#picGuess").hasClass("d-none")) {
			$("#picGuess").addClass("d-none");
		}
		if ($("#pictureContainer").hasClass("d-none")) {
			$("#pictureContainer").removeClass("d-none");
		}
		for (var segment in data.segments) {
			if (!$("#img" + data.segments[segment]).hasClass("segment-used")) {
				  $( "#img" + data.segments[segment]).addClass( "segment-used");
			}
		}
	}
	
	if (data.gameStatus == "FINISHED") {
		if (data.gameResult == "WON") {
			if ($("#successAlert").hasClass("d-none")) {
				$("#successAlert").removeClass("d-none");
			} 
		} else if (data.gameResult == "LOST") {
			if ($("#lostAlert").hasClass("d-none")) {
				$("#lostAlert").removeClass("d-none");
			} 			
		}
		if (!$("#picGuess").hasClass("d-none")) {
			$("#picGuess").addClass("d-none");
		}			
		if (!$("#sendSegment").hasClass("d-none")) {
			$("#sendSegment").addClass("d-none");
		}				
		if ($("#gameOverDiv").hasClass("d-none")) {
			  $( "#gameOverDiv").removeClass( "d-none");
		}
	}		
		
	$canWidth = ($("#img0").width() > 100 ) ? $("#img0").width() : 500;
	$canHeight = ($("#img0").height() > 100 ) ? $("#img0").height() : 500;
	
	$('#myCanvas').css({
        'width': $canWidth + 'px', 
        'height': $canHeight +'px'
    });
	setTimeout(getGameInfo,500);
	});
}
