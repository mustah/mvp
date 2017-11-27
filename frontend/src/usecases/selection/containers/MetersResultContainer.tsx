import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {PaginationControl} from '../../../components/pagination-control/PaginationControl';
import {NormalizedRows} from '../../../components/table/Table';
import {RootState} from '../../../reducers/rootReducer';
import {getResultDomainModels} from '../../../state/domain-models/domainModelsSelectors';
import {getMeterEntities, getMetersTotal} from '../../../state/domain-models/meter/meterSelectors';
import {changePaginationSelection} from '../../../state/ui/pagination/paginationActions';
import {ChangePage, Pagination} from '../../../state/ui/pagination/paginationModels';
import {getPaginationList, getSelectionPagination} from '../../../state/ui/pagination/paginationSelectors';
import {OnClickWithId} from '../../../types/Types';
import {selectEntryAdd} from '../../report/reportActions';
import {SearchResultList} from '../components/SelectionResultList';

interface StateToProps {
  pagination: Pagination;
  numOfEntities: number;
  meters: NormalizedRows;
}

interface DispatchToProps {
  changePage: ChangePage;
  selectEntryAdd: OnClickWithId;
}

const MetersComponent = (props: StateToProps & DispatchToProps) => {
  const {meters, changePage, numOfEntities, pagination, selectEntryAdd} = props;
  return (
    <div>
      <SearchResultList data={meters} selectEntryAdd={selectEntryAdd}/>

      <PaginationControl
        changePage={changePage}
        numOfEntities={numOfEntities}
        pagination={pagination}
      />
    </div>
  );
};
const mapStateToProps = ({ui, domainModels: {meters}}: RootState): StateToProps => {
  const pagination = getSelectionPagination(ui);
  return {
    numOfEntities: getMetersTotal(meters),
    meters: {
      allIds: getPaginationList({pagination, result: getResultDomainModels(meters)}),
      byId: getMeterEntities(meters),
    },
    pagination,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  changePage: changePaginationSelection,
  selectEntryAdd,
}, dispatch);

export const MetersResultContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(MetersComponent);
