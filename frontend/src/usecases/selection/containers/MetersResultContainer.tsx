import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {getMeterEntities, getMetersTotal} from '../../../state/domain-models/meter/meterSelectors';
import {changePaginationSelection} from '../../../state/ui/pagination/paginationActions';
import {ChangePage, Pagination} from '../../../state/ui/pagination/paginationModels';
import {getPaginationList, getSelectionPagination} from '../../../state/ui/pagination/paginationSelectors';
import {PaginationControl} from '../../common/components/pagination-control/PaginationControl';
import {NormalizedRows} from '../../common/components/table/Table';
import {SearchResultList} from '../components/SelectionResultList';
import {getResultDomainModels} from '../../../state/domain-models/domainModelsSelectors';

interface StateToProps {
  pagination: Pagination;
  numOfEntities: number;
  meters: NormalizedRows;
}

interface DispatchToProps {
  changePage: ChangePage;
}

const MetersComponent = (props: StateToProps & DispatchToProps) => {
  const {meters, changePage, numOfEntities, pagination} = props;
  return (
    <div>
      <SearchResultList data={meters}/>

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
}, dispatch);

export const MetersResultContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(MetersComponent);
