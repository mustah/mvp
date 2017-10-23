import * as React from 'react';
import 'SearchResultList.scss';
import {translate} from '../../../services/translationService';
import {Column} from '../../common/components/layouts/column/Column';
import {Row} from '../../common/components/layouts/row/Row';
import {Summary} from '../../common/components/summary/Summary';
import {MeteringPoint} from '../../common/components/table/meteringPoint/MeteringPoint';
import {Status} from '../../common/components/table/status/Status';
import {Table} from '../../common/components/table/table/Table';
import {TableHead} from '../../common/components/table/table/TableHead';
import {TableColumn} from '../../common/components/table/tableColumn/TableColumn';
import {normalizedData} from '../../dashboard/models/dashboardModels';

const renderMeteringPointCell = (value, index) => <MeteringPoint id={value}/>;
const renderStatusCell = (value, index) => <Status code={value.code} content={value.text}/>;

export const SearchResultList = (props) => (
  <Column className="SearchResultList">
    <Row className="SearchResultList-Summary">
      <Row className="Summaries">
        <Summary title="Städer" count="1"/>
        <Summary title="Adresser" count="2"/>
        <Summary title="Mätpunkter" count="4"/>
      </Row>
    </Row>
    <Table data={normalizedData.meteringPoints}>
      <TableColumn
        id={'id'}
        header={<TableHead>{translate('meter')}</TableHead>}
        cell={renderMeteringPointCell}
      />
      <TableColumn
        id={'type'}
        header={<TableHead>{translate('type')}</TableHead>}
      />
      <TableColumn
        id={'location'}
        header={<TableHead>{translate('location')}</TableHead>}
      />
      <TableColumn
        id={'gateway'}
        header={<TableHead>{translate('gateway')}</TableHead>}
      />
      <TableColumn
        id={'status'}
        header={<TableHead sortable={true} currentSort={'asc'}>{translate('status')}</TableHead>}
        cell={renderStatusCell}
      />
    </Table>
  </Column>
);
