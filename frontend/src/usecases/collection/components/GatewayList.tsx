import * as React from 'react';
import {ListActionsDropdown} from '../../../components/actions-dropdown/ListActionsDropdown';
import {RowRight} from '../../../components/layouts/row/Row';
import {PaginationControl} from '../../../components/pagination-control/PaginationControl';
import {Status} from '../../../components/status/Status';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
import {TableInfoText} from '../../../components/table/TableInfoText';
import {orUnknown} from '../../../helpers/translations';
import {translate} from '../../../services/translationService';
import {Gateway} from '../../../state/domain-models-paginated/gateway/gatewayModels';
import {GatewayListProps} from '../containers/GatewayListContainer';
import {GatewayListItem} from './GatewayListItem';

export const GatewayList = ({
  changePage,
  componentId,
  result,
  entities,
  selectEntryAdd,
  pagination,
  entityType,
}: GatewayListProps) => {

  const renderGatewayListItem = (gateway: Gateway) => <GatewayListItem gateway={gateway}/>;
  const renderStatusCell = ({status: {name}}: Gateway) => <Status name={name}/>;
  const renderCity = ({location: {city}}: Gateway) => orUnknown(city.name);
  const renderAddress = ({location: {address}}: Gateway) => orUnknown(address.name);
  const renderActions = ({id, productModel}: Gateway) => (
    <RowRight className="ActionsDropdown-list">
      <ListActionsDropdown item={{id, name: productModel}} selectEntryAdd={selectEntryAdd}/>
    </RowRight>
  );
  const renderProductModel = ({productModel}: Gateway) => productModel;

  const onChangePage = (page: number) => changePage({entityType, componentId, page});

  return (
    <div>
      <Table result={result} entities={entities}>
        <TableColumn
          cellClassName="icon"
          header={<TableHead className="first">{translate('gateway')}</TableHead>}
          renderCell={renderGatewayListItem}
        />
        <TableColumn
          header={<TableHead>{translate('city')}</TableHead>}
          cellClassName={'first-uppercase'}
          renderCell={renderCity}
        />
        <TableColumn
          header={<TableHead>{translate('address')}</TableHead>}
          cellClassName={'first-uppercase'}
          renderCell={renderAddress}
        />
        <TableColumn
          header={<TableHead>{translate('product model')}</TableHead>}
          renderCell={renderProductModel}
        />
        <TableColumn
          header={<TableHead className="TableHead-status">{translate('status')}</TableHead>}
          renderCell={renderStatusCell}
        />
        <TableColumn
          header={<TableHead/>}
          renderCell={renderActions}
        />
      </Table>
      <TableInfoText/>
      <PaginationControl pagination={pagination} changePage={onChangePage}/>
    </div>
  );
};
