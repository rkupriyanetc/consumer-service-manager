
topAddrChanged = ->
  jsRoutes.mk.ck.energy.csm.controllers.Account.onChangeAddressTopSelect($('#topAddress').val()).ajax
    method: "get"
    success: (data) ->
      $('#locationAddress').html data
      return
    error: (err) ->
      alert "Error #{err}"
      return
  true

formatState = (state) ->
  if state.id
    $state = $("<span><img src=\"vendor/images/flags/#{state.element.value}.png\" class=\"img-flag\" /> #{state.text}</span>")
    $state
  else
    state.text

$('#topAddress').change -> topAddrChanged this

$("#topAddress").select2
  templateResult: formatState

$("#locationAddress").select2
  templateResult: formatState

$("#placeAddress").select2
  templateResult: formatState