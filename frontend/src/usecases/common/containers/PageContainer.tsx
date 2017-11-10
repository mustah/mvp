import * as classNames from 'classnames';
import {Pathname} from 'history';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {getPathname, isSearchPage} from '../../../selectors/routerSelectors';
import {selectPeriod} from '../../../state/search/selection/selectionActions';
import {OnSelectPeriod, SelectionState} from '../../../state/search/selection/selectionModels';
import {getSelectedPeriod, getSelection} from '../../../state/search/selection/selectionSelectors';
import {isSideMenuOpen} from '../../../state/ui/uiSelectors';
import {Period} from '../../../types/Types';
import {SelectionMenuSummary} from '../../selection/components/selection-menu/SelectionMenuSummary';
import {SearchMenuWrapper} from '../../selection/components/selection-menu/SelectionMenuWrapper';
import {SelectionMenuContainer} from '../../selection/containers/SelectionMenuContainer';
import {Column} from '../components/layouts/column/Column';
import {Layout} from '../components/layouts/layout/Layout';

interface StateToProps {
  pathname: Pathname;
  isSearchPage: boolean;
  isSideMenuOpen: boolean;
  children?: React.ReactNode;
  selectedPeriod: Period;
  selection: SelectionState;
}

interface DispatchToProps {
  selectPeriod: OnSelectPeriod;
}

const PageContainerComponent = (props: StateToProps & DispatchToProps) => {
  const {
    children,
    selection,
    isSearchPage,
    isSideMenuOpen,
    pathname,
    selectPeriod,
    selectedPeriod,
  } = props;

  const renderSelectionSearch = isSearchPage
    ? <SelectionMenuContainer/>
    : <SelectionMenuSummary pathname={pathname} selection={selection}/>;

  return (
    <Layout>
      <SearchMenuWrapper
        period={selectedPeriod}
        className={classNames({isSideMenuOpen})}
        selectPeriod={selectPeriod}
      >
        {renderSelectionSearch}
      </SearchMenuWrapper>

      <Column className="flex-1 PageContent">
        {children}
      </Column>
    </Layout>
  );
};

const mapStateToProps = ({routing, ui, searchParameters}: RootState): StateToProps => {
  return {
    selection: getSelection(searchParameters),
    isSearchPage: isSearchPage(routing),
    pathname: getPathname(routing),
    isSideMenuOpen: isSideMenuOpen(ui),
    selectedPeriod: getSelectedPeriod(searchParameters.selection),
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  selectPeriod,
}, dispatch);

export const PageContainer =
  connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(PageContainerComponent);
