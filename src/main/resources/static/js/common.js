$(function() {

	$(".table").DataTable({
		scrollY : "450px",
		scrollCollapse : true,
		url : "//cdn.datatables.net/plug-ins/3cfcc339e89/i18n/Japanese.json",
		info : false,
		paging : false,
		searching : false,
		ordering : false
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
