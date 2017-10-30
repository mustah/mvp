import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {RootState} from '../../../reducers/rootReducer';
import {Meter} from '../../../state/domain-models/meter/meterModels';
import {getMeterEntities, getMetersTotal} from '../../../state/domain-models/meter/meterSelectors';
import {toggleSelection} from '../../../state/search/selection/selectionActions';
import {SelectionParameter} from '../../../state/search/selection/selectionModels';
import {SelectionState} from '../../../state/search/selection/selectionReducer';
import {uuid} from '../../../types/Types';
import {PageContainer} from '../../common/containers/PageContainer';
import {paginationChangePage} from '../../ui/pagination/paginationActions';
import {Pagination} from '../../ui/pagination/paginationModels';
import {getPaginationList, getSelectionPagination} from '../../ui/pagination/paginationSelectors';
import {SelectionContentBox} from '../components/SelectionContentBox';
import {SelectionOptionsLoaderContainer} from './SelectionOptionsLoaderContainer';

export interface SelectionStateToProps {
  selection: SelectionState;
  numOfMeters: number;
  meters: {[key: string]: Meter};
  paginatedList: uuid[];
  pagination: Pagination;
}

export interface SelectionDispatchToProps {
  toggleSelection: (searchParameters: SelectionParameter) => void;
  paginationChangePage: (payload: {page: number; useCase: string; }) => any;
}

type Props = SelectionStateToProps & SelectionDispatchToProps & InjectedAuthRouterProps;

const SelectionContainerComponent = (props: Props) => {
  const {selection, toggleSelection, numOfMeters, meters, paginatedList, pagination} = props;
  const SELECTION = 'SELECTION';
  const onChangePagination = (page: number) => {
    paginationChangePage({
      page,
      useCase: SELECTION,
    });
  };
  return (
    <PageContainer>
      <SelectionOptionsLoaderContainer>
        <SelectionContentBox
          selection={selection}
          toggleSelection={toggleSelection}
          data={{allIds: paginatedList, byId: meters}}
          pagination={pagination}
          numOfEntities={numOfMeters}
          changePage={onChangePagination}
        />
      </SelectionOptionsLoaderContainer>
    </PageContainer>
  );
};

const mapStateToProps = ({selection, ui, domainModels}: RootState): SelectionStateToProps => {
  const pagination = getSelectionPagination(ui);
  const meters = domainModels.meters;
  return {
    selection,
    numOfMeters: getMetersTotal(meters),
    meters: getMeterEntities(meters),
    paginatedList: getPaginationList({...pagination, ...meters}),
    pagination,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  toggleSelection,
  paginationChangePage,
}, dispatch);

export const SelectionContainer =
  connect<SelectionStateToProps, SelectionDispatchToProps, {}>
  (mapStateToProps, mapDispatchToProps)(SelectionContainerComponent);
