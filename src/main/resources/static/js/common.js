$(function() {

    $(".table").DataTable({
        scrollY : "500px",
        scrollCollapse : true,
    });

    $(".dataTables_scrollBody").each(function(i, e) {

        var max = $(this).css("max-height");
        $(this).css("min-height", max);

    });

    var form;

    $(".delete-form form").on("submit", function() {

        form = $(this);
        $('#delete_confime').modal('show');
        return false;
    });


    $("#delete_confime .btn-primary").on("click", function() {

        form.off("submit");
        form.submit();

    });

});
