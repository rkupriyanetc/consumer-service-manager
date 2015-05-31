select top 2 m.nazva_marka, n.nomer, n.razr, n.n_date, n.k_date, i.inspektor, n.doc, n.amp, ma.code_mestoacc, n.date_pov from _accnastr n
left join _abonent a on a.code_ab = n.code_ab
left join sl_marka_acc m on m.code_marka = n.code_marka
left join _tonastr tn on tn.code_ab = n.code_ab and tn.code_to = n.code_to
left join _mestoacc ma on ma.code_ab = n.code_ab
left join _sl_insp i on i.code_insp = n.code_insp
where a.code_abon = ? and n.code_to > 0 and tn.code_status = 1 and 
  substring( tn.code_tar, 4, 3 ) in ( '201', '205', '211', '215', '261', '265' )
order by n.n_date desc