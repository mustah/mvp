import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {Flag} from '../../../../state/domain-models/flag/flagModels';
import {IdNamed} from '../../../../types/Types';
import {MeteringPoint} from './MeteringPoint';
import {ActionsDropdown} from '../actions-dropdown/ActionsDropdown';
import {Status} from '../status/Status';
import {ListProps} from '../tabs/models/TabsModel';
import {Table} from '../table/Table';
import {TableColumn} from '../table/TableColumn';
import {TableHead} from '../table/TableHead';

export const MeterList = (props: ListProps) => {
  const {data} = props;
  const renderMeteringPointCell = (value, index) => <MeteringPoint id={value}/>;
  const renderStatusCell = (status: IdNamed) => <Status {...status}/>;
  const renderLocation = (value: IdNamed) => value.name;
  const renderFlags = (flags: Flag[]) => flags.map((flag) => flag.title).join(', ');

  const dropdownActions = [
    translate('export to Excel (.csv)'),
    translate('export to JSON'),
    translate('add to report'),
  ];

  const renderActionDropdown = () => <ActionsDropdown actions={dropdownActions}/>;

  return (
    <Table data={data}>
      <TableColumn
        id={'id'}
        header={<TableHead className="first">{translate('meter')}</TableHead>}
        cell={renderMeteringPointCell}
      />
      <TableColumn
        id={'city'}
        header={<TableHead>{translate('city')}</TableHead>}
        cell={renderLocation}
      />
      <TableColumn
        id={'address'}
        header={<TableHead>{translate('address')}</TableHead>}
        cell={renderLocation}
      />
      <TableColumn
        id={'manufacturer'}
        header={<TableHead>{translate('manufacturer')}</TableHead>}
      />
      <TableColumn
        id={'medium'}
        header={<TableHead>{translate('medium')}</TableHead>}
      />
      <TableColumn
        id={'gatewayId'}
        header={<TableHead>{translate('gateway')}</TableHead>}
      />
      <TableColumn
        id={'status'}
        header={<TableHead className="TableHead-status">{translate('status')}</TableHead>}
        cell={renderStatusCell}
      />
      <TableColumn
        id={'statusChanged'}
        header={<TableHead sortable={true} currentSort="desc">{translate('status change')}</TableHead>}
      />
      <TableColumn
        id={'flags'}
        header={<TableHead>{translate('flags')}</TableHead>}
        cell={renderFlags}
      />
      <TableColumn
        id={'action-buttons'}
        header={<TableHead className="actionDropdown">{' '}</TableHead>}
        cell={renderActionDropdown}
      />
    </Table>
  );
};
