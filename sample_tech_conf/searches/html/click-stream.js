//var clickstreamUrl = 'http://localhost:8983/solr/bh-cs/';
var clickstreamUrl = 'http://dc2-fusion1.bnh.com:8983/solr/bh_clickstream/';

function searchClickStream(term) {
	var url = clickstreamUrl + 'browse?wt=json&' + $('#searchForm').serialize();
	$.get(url, function(data) {
		var jdata = JSON.parse(data);
		html = cardTemplate(jdata.response);
		document.getElementById("cards").innerHTML = html;
		var facets = {};
		Object.keys(jdata.facet_counts.facet_fields).forEach((f) => {
			facets[f] = convertArrayToPairs(jdata.facet_counts.facet_fields[f]);
		});
		html = facetTemplate(facets);
		document.getElementById("facetFields").innerHTML = html;
		$('#sidebar ul li ul li a').on("click", function () {
			$('#searchForm input[name=fq]').val($(this).data('term'));
			searchClickStream();
		})

		var fq = jdata.responseHeader.params.fq;
		if(fq) {
			$('#selectedFacets').empty();
			if (Array.isArray(fq)) {
				fq.forEach( (fq) => $('#selectedFacets').innerHTML = $('#selectedFacets').innerHTML + fq);
			} else {
				$('#selectedFacets').append('<a href="#" class="fq" name='+ fq.replace(/"/g,'') +'>' + fq + '</a>');
			}
			$('#selectedFacetContainer').toggle(true);
		} else {
			$('#selectedFacetContainer').toggle(false);
		}

		$('a[class=fq]').on('click', function(event) {
			$('#searchForm input[name=fq]').val('');
			$(this).remove();
			searchClickStream();
		});
	});
}

function convertArrayToPairs(array) {
	var dictionary = {};

	array.forEach(function(item, index) {
		if(index % 2 === 0) {
		dictionary[item] = array[index + 1];
		}
	});
	return dictionary;
}

var cardTemplate;
var facetTemplate;
var searchesTemplate;

$(document).ready(function() {
	var source = document.getElementById("item-template").innerHTML;
	cardTemplate = Handlebars.compile(source);
	source = document.getElementById("facet-template").innerHTML;
	facetTemplate = Handlebars.compile(source);
	source = document.getElementById("searches-template").innerHTML;
	searchesTemplate = Handlebars.compile(source);
	
	$('#searchForm').submit(function(event) {
		event.preventDefault();
		searchClickStream();
		return false;
	});

	$('#searchForm input[name=sort]').on("click", function () {
		searchClickStream();
	});
	searchClickStream();

	$('#itemSearchesModal').on('show.bs.modal', function (event) {
		var url = clickstreamUrl + 'select?fl=modified_*,original_term,display_name&q=*:*&sort=modified_detail_pages%20desc&rows=30&fq={!collapse%20field=modified_term}&fq=sku_no:';
		var button = $(event.relatedTarget) // Button that triggered the modal
		var skuno = button.data('skuno') // Extract info from data-* attributes
		$.get(url+skuno, (data) => {
			var modal = $(this)
			var displayName = data.response.docs[0].display_name;
			modal.find('.modal-title').text('Searches leading to: ' + displayName);
			modal.find('.modal-body').empty();
			modal.find('.modal-body').append(searchesTemplate(data.response));
		})
		//modal.find('.modal-body input').val(recipient)
	})

});
