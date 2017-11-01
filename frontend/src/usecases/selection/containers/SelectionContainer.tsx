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
import {changePaginationSelection} from '../../ui/pagination/paginationActions';
import {Pagination} from '../../ui/pagination/paginationModels';
import {getPaginationList, getSelectionPagination} from '../../ui/pagination/paginationSelectors';
import {SelectionContentBox} from '../components/SelectionContentBox';
import {getSelection} from '../../../state/search/selection/selectionSelectors';
import {PageContainer} from '../../common/containers/PageContainer';
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
  paginationChangePage: (page: number) => any;
}

type Props = SelectionStateToProps & SelectionDispatchToProps & InjectedAuthRouterProps;

const SelectionContainerComponent = (props: Props) => {
  const {selection, toggleSelection, numOfMeters, meters, paginatedList, pagination, paginationChangePage} = props;
  return (
    <PageContainer>
      <SelectionOptionsLoaderContainer>
        <SelectionContentBox
          selection={selection}
          toggleSelection={toggleSelection}
          data={{allIds: paginatedList, byId: meters}}
          paginationProps={{pagination, changePage: paginationChangePage, numOfEntities: numOfMeters}}
        />
      </SelectionOptionsLoaderContainer>
    </PageContainer>
  );
};

const mapStateToProps = ({searchParameters, ui, domainModels}: RootState): SelectionStateToProps => {
  const pagination = getSelectionPagination(ui);
  const meters = domainModels.meters;
  return {
    selection: getSelection(searchParameters),
    numOfMeters: getMetersTotal(meters),
    meters: getMeterEntities(meters),
    paginatedList: getPaginationList({...pagination, ...meters}),
    pagination,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  toggleSelection,
  paginationChangePage: changePaginationSelection,
}, dispatch);

export const SelectionContainer =
  connect<SelectionStateToProps, SelectionDispatchToProps, {}>
  (mapStateToProps, mapDispatchToProps)(SelectionContainerComponent);
