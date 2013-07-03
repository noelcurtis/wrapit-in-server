$(function(){

    // listen for Return on the Comments field
    $("#item-detail-comment").keyup(function(event){
        if (event.which == 13) {
            event.preventDefault();
            itemDetailSubmitComment(itdGiftListId, itdItemID, $(this).val(), itdUserEmail);
        }
    })

});

// Use to submit a comment via ajax
function itemDetailSubmitComment(giftListId, itemId, comment, email) {
    if (typeof comment == "undefined" || comment == "") return;
    // submit the comment via post
    var data = {comment: comment}
    $.post("/list/"+giftListId+"/items/"+itemId+"/comment", data).done(function(data) {
        console.log(data)
        if (data.status == "ok")
        {
            $("#item-detail-comment").val(""); // clear the field
            var clone = $(".comment-cell").first().clone(); // create a comment cell
            $(clone).find(".c-text").html(comment);
            $(clone).find(".c-author").html(email);
            $(clone).appendTo(".nav-list.item-detail").show(); // append the cell
        }
    }).fail(function(data) {
        console.log(data);
    });
}


function itemDetailUpdatePurchased(giftListId, itemId, purchased) {
    if (typeof purchased == "undefined") return;

    console.log(purchased)


    var data = {purchased: purchased}
    $.post("/list/"+giftListId+"/items/"+itemId+"/purchased", data).done(function(data) {
        console.log(data)
        if (data.status = "ok")
        {
            var purchased = data.purchased;
            if (purchased == 1)
            {
                $("#item-detail-purchased").hide(); // toggle buttons
                $(".buy-badge.bought").show({duration: 100});
                $("#item-detail-available").show();
                $(".buy-badge.available").hide({duration: 100});
            }
            else
            {
                $("#item-detail-purchased").show(); // toggle buttons
                $(".buy-badge.bought").hide({duration: 100});
                $("#item-detail-available").hide();
                $(".buy-badge.available").show({duration: 100});
            }
        }
    }).fail(function(data) {
        console.log(data);
    });

}