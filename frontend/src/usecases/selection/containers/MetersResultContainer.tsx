import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Loader} from '../../../components/loading/Loader';
import {PaginationControl} from '../../../components/pagination-control/PaginationControl';
import {RootState} from '../../../reducers/rootReducer';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {getResultDomainModels} from '../../../state/domain-models/domainModelsSelectors';
import {Meter} from '../../../state/domain-models/meter/meterModels';
import {getMeterEntities, getMetersTotal} from '../../../state/domain-models/meter/meterSelectors';
import {changePaginationSelection} from '../../../state/ui/pagination/paginationActions';
import {OnChangePage, Pagination} from '../../../state/ui/pagination/paginationModels';
import {getPaginationList, getSelectionPagination} from '../../../state/ui/pagination/paginationSelectors';
import {OnClickWithId, uuid} from '../../../types/Types';
import {selectEntryAdd} from '../../report/reportActions';
import {SearchResultList} from '../components/SelectionResultList';

interface StateToProps {
  isFetching: boolean;
  pagination: Pagination;
  numOfEntities: number;
  meters: DomainModel<Meter>;
  paginatedList: uuid[];
}

interface DispatchToProps {
  changePage: OnChangePage;
  selectEntryAdd: OnClickWithId;
}

const MetersComponent = (props: StateToProps & DispatchToProps) => {
  const {isFetching, meters, paginatedList, changePage, numOfEntities, pagination, selectEntryAdd} = props;
  return (
    <Loader isFetching={isFetching}>
      <div>
        <SearchResultList result={paginatedList} entities={meters} selectEntryAdd={selectEntryAdd}/>

        <PaginationControl
          changePage={changePage}
          numOfEntities={numOfEntities}
          pagination={pagination}
        />
      </div>
    </Loader>
  );
};
const mapStateToProps = ({ui, domainModels: {meters}}: RootState): StateToProps => {
  const pagination = getSelectionPagination(ui);
  return {
    isFetching: meters.isFetching,
    numOfEntities: getMetersTotal(meters),
    meters: getMeterEntities(meters),
    paginatedList: getPaginationList({pagination, result: getResultDomainModels(meters)}),
    pagination,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  changePage: changePaginationSelection,
  selectEntryAdd,
}, dispatch);

export const MetersResultContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(MetersComponent);
