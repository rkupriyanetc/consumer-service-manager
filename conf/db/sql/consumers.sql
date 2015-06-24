select a.code_abon, w.surname, t.nazva_pos, s.nazva_street, a.house, a.flat, a.indeks, a.privat, sa.code_status, a.unicod, a.doc, w.doc from _abonent a
left join _sl_respos t on t.code_pos = a.code_pos
left join _sl_streets s on s.code_street = a.code_street
left join _owner w on w.code_ab = a.code_ab
left join _status_ab sa on sa.code_ab = a.code_ab
where sa.code_status not in ( 2, 80 ) and --a.code_abon in ( '07-000669', '37-002569', '03-000441', '08-000802', '26-002086', '02-000227', '01-000189', '56-003945', '01-000089', '01-000063', '01-000002', '37-002608', '13-001245', '37-002689', '35-002463', '250648', '250660', '01-003706' ) and
  sa.n_date = ( select max( n_date ) from _status_ab where code_ab = a.code_ab ) and
    w.n_date = ( select max( n_date ) from _owner where code_ab = a.code_ab )
order by t.nazva_pos, a.code_abon