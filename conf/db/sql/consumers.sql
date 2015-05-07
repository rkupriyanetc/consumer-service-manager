declare
  @codeab     int,
  @marka      varchar( 30 ),
  @codemesto  tinyint,
  @inspektor  varchar( 20 ),
  @nomer      varchar( 18 ),
  @rozr       tinyint,
  @ndate      smalldatetime,
  @doc        varchar( 15 ),
  @amp        smallint,
  @coupl      int,
  @number_pl  varchar( 100 ),
  @ndate_pl   smalldatetime,
  @kdate_pl   smalldatetime,
  @insp_pl    varchar( 20 ),
  @code_pl    int;

create table #_RVK_tmp_InfoConsumers (
  id          int not null,
  lsid        varchar( 12 ) collate SQL_Latin1_General_CP1251_CI_AS,
  nazvapos    varchar( 30 ) collate SQL_Latin1_General_CP1251_CI_AS,
  nstreet     varchar( 40 ) collate SQL_Latin1_General_CP1251_CI_AS,
  house       varchar( 10 ) collate SQL_Latin1_General_CP1251_CI_AS,
  flat        varchar( 4 ) collate SQL_Latin1_General_CP1251_CI_AS,
  indeks      int,
  uid         varchar( 15 ) collate SQL_Latin1_General_CP1251_CI_AS,
  adoc        varchar( 30 ) collate SQL_Latin1_General_CP1251_CI_AS,
  codestatus  tinyint,
  surname     varchar( 40 ) collate SQL_Latin1_General_CP1251_CI_AS,
  wdoc        varchar( 20 ) collate SQL_Latin1_General_CP1251_CI_AS,
  priv        bit,
  marka       varchar( 30 ) collate SQL_Latin1_General_CP1251_CI_AS,
  codemesto   tinyint,
  inspektor   varchar( 20 ) collate SQL_Latin1_General_CP1251_CI_AS,
  nomer       varchar( 18 ) collate SQL_Latin1_General_CP1251_CI_AS,
  rozr        tinyint,
  ndate       smalldatetime,
  cdoc        varchar( 15 ) collate SQL_Latin1_General_CP1251_CI_AS,
  amp         smallint,
  number_pl   varchar( 100 ) collate SQL_Latin1_General_CP1251_CI_AS,
  ndate_pl    smalldatetime,
  kdate_pl    smalldatetime,
  insp_pl     varchar( 20 ) collate SQL_Latin1_General_CP1251_CI_AS,
  code_pl     int,
  number_pl2  varchar( 100 ) collate SQL_Latin1_General_CP1251_CI_AS,
  ndate_pl2   smalldatetime,
  kdate_pl2   smalldatetime,
  insp_pl2    varchar( 20 ) collate SQL_Latin1_General_CP1251_CI_AS,
  code_pl2    int,
  count_plumb tinyint
);
insert into #_RVK_tmp_InfoConsumers ( id, lsid, nazvapos, nstreet, house, flat, indeks, uid, adoc, priv, codestatus, surname, wdoc )
select a.code_ab, a.code_abon, t.nazva_pos, s.nazva_street, a.house, a.flat, a.indeks, a.unicod, a.doc, a.privat,
  sa.code_status, w.surname, w.doc from _abonent a
left join _sl_respos t on t.code_pos = a.code_pos
left join _sl_streets s on s.code_street = a.code_street
left join _owner w on w.code_ab = a.code_ab
left join _status_ab sa on sa.code_ab = a.code_ab
where sa.code_status not in ( 2, 80 ) and t.code_pos = 1 and --a.code_abon in ( '250648', '250660', '01-003706', '35-002463' ) and
  sa.n_date = ( select max( n_date ) from _status_ab where code_ab = a.code_ab ) and
    w.n_date = ( select max( n_date ) from _owner where code_ab = a.code_ab )
order by t.nazva_pos, a.code_abon;

declare consumers cursor for
select id from #_RVK_tmp_InfoConsumers;

open consumers;
fetch next from consumers into @codeab;

while ( @@fetch_status <> - 1 ) begin
  set @marka = null
  set @codemesto = null
  set @inspektor = null
  set @nomer = null
  set @rozr = null
  set @ndate = null
  set @doc = null
  set @amp = null
    
  select @marka = m.nazva_marka, @codemesto = ma.code_mestoacc, @inspektor = i.inspektor, 
  @nomer = n.nomer, @rozr = n.razr, @ndate = n.n_date, @doc = n.doc, @amp = n.amp from _accnastr n
  left join sl_marka_acc m on m.code_marka = n.code_marka
  left join _tonastr tn on tn.code_ab = n.code_ab and tn.code_to = n.code_to
  left join _mestoacc ma on ma.code_ab = n.code_ab
  left join _sl_insp i on i.code_insp = n.code_insp
  where n.code_ab = @codeab and n.k_date > getdate() and n.code_to > 0 and tn.code_status = 1 and
    n.code_acc = ( select min( code_acc ) from _accnastr
      where k_date > getdate() and code_to > 0 and code_ab = n.code_ab and code_acc >= code_ab )
  
  if ( @marka is null ) begin
    delete from #_RVK_tmp_InfoConsumers
    where id = @codeab
  end
  else begin
    update #_RVK_tmp_InfoConsumers 
      set marka = @marka, codemesto = @codemesto, inspektor = @inspektor, nomer = @nomer, rozr = @rozr, ndate = @ndate, cdoc = @doc, amp = @amp
      where id = @codeab
  end

  declare plumbs_tmp cursor for
  select plomba, n_date, k_date, prim, type_plb from _plomba
    where code_ab = @codeab and k_date > getdate()
    
  open plumbs_tmp
  set @coupl = 0
  
  fetch next from plumbs_tmp into @number_pl, @ndate_pl, @kdate_pl, @insp_pl, @code_pl
  
  while ( @@fetch_status <> - 1 ) begin
    if ( @coupl = 0 ) begin
      update #_RVK_tmp_InfoConsumers 
        set number_pl = @number_pl, ndate_pl = @ndate_pl, 
        kdate_pl = @kdate_pl, insp_pl = @insp_pl, code_pl = @code_pl
        where id = @codeab
    end
    else begin
      update #_RVK_tmp_InfoConsumers 
        set number_pl2 = @number_pl, ndate_pl2 = @ndate_pl, 
        kdate_pl2 = @kdate_pl, insp_pl2 = @insp_pl, code_pl2 = @code_pl
        where id = @codeab
    end
     
    set @coupl = @coupl + 1
    set @number_pl = null
    set @ndate_pl = null
    set @kdate_pl = null
    set @insp_pl = null
    set @code_pl = null
    
    fetch next from plumbs_tmp into @number_pl, @ndate_pl, @kdate_pl, @insp_pl, @code_pl
  end
  
  update #_RVK_tmp_InfoConsumers 
    set count_plumb = @coupl
    where id = @codeab
  
  close plumbs_tmp
  deallocate plumbs_tmp
  
  fetch next from consumers into @codeab
end;
close consumers;
deallocate consumers;

select lsid, nazvapos, nstreet, house, flat, indeks, uid, adoc, codestatus, surname, wdoc, priv, marka, codemesto, 
  inspektor, nomer, rozr, ndate, cdoc, amp, number_pl, ndate_pl, kdate_pl, insp_pl, code_pl, 
  number_pl2, ndate_pl2, kdate_pl2, insp_pl2, code_pl2, count_plumb from #_RVK_tmp_InfoConsumers;

drop table #_RVK_tmp_InfoConsumers;