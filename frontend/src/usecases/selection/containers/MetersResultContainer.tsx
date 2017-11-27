import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {PaginationControl} from '../../../components/pagination-control/PaginationControl';
import {RootState} from '../../../reducers/rootReducer';
import {getResultDomainModels} from '../../../state/domain-models/domainModelsSelectors';
import {getMeterEntities, getMetersTotal} from '../../../state/domain-models/meter/meterSelectors';
import {changePaginationSelection} from '../../../state/ui/pagination/paginationActions';
import {ChangePage, Pagination} from '../../../state/ui/pagination/paginationModels';
import {getPaginationList, getSelectionPagination} from '../../../state/ui/pagination/paginationSelectors';
import {OnClickWithId, uuid} from '../../../types/Types';
import {selectEntryAdd} from '../../report/reportActions';
import {SearchResultList} from '../components/SelectionResultList';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {Meter} from '../../../state/domain-models/meter/meterModels';

interface StateToProps {
  pagination: Pagination;
  numOfEntities: number;
  meters: DomainModel<Meter>;
  paginatedList: uuid[];
}

interface DispatchToProps {
  changePage: ChangePage;
  selectEntryAdd: OnClickWithId;
}

const MetersComponent = (props: StateToProps & DispatchToProps) => {
  const {meters, paginatedList, changePage, numOfEntities, pagination, selectEntryAdd} = props;
  return (
    <div>
      <SearchResultList result={paginatedList} entities={meters} selectEntryAdd={selectEntryAdd}/>

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
