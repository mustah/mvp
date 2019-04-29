import * as React from 'react';
import {PaginationControl} from '../../../components/pagination-control/PaginationControl';
import {Status} from '../../../components/status/Status';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
import {orUnknown} from '../../../helpers/translations';
import {translate} from '../../../services/translationService';
import {Gateway} from '../../../state/domain-models-paginated/gateway/gatewayModels';
import {GatewayListProps} from '../containers/GatewayListContainer';
import {GatewayListItem} from './GatewayListItem';

export const GatewayList = ({
  changePage,
  result,
  entities,
  pagination,
  entityType,
}: GatewayListProps) => {

  const renderGatewayListItem = (gateway: Gateway) => <GatewayListItem gateway={gateway}/>;
  const renderCollectionStatus = ({status: {name}}: Gateway) => <Status label={name}/>;
  const renderCity = ({location: {city}}: Gateway) => orUnknown(city);
  const renderAddress = ({location: {address}}: Gateway) => orUnknown(address);
  const renderProductModel = ({productModel}: Gateway) => productModel;

  const onChangePage = (page: number) => changePage({entityType, page});

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
          header={<TableHead>{translate('collection')}</TableHead>}
          renderCell={renderCollectionStatus}
        />
      </Table>
      <PaginationControl pagination={pagination} changePage={onChangePage}/>
    </div>
  );
};
