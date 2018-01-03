import * as React from 'react';
import {connect} from 'react-redux';
import {RootState} from '../reducers/rootReducer';
import {isSelectionPage} from '../selectors/routerSelectors';
import {SelectionState} from '../state/search/selection/selectionModels';
import {getSelection} from '../state/search/selection/selectionSelectors';
import {isSideMenuOpen} from '../state/ui/uiSelectors';
import {SelectionMenuSummary} from '../usecases/selection/components/selection-menu/SelectionMenuSummary';
import {SelectionMenuContainer} from '../usecases/selection/containers/SelectionMenuContainer';
import {PageComponent} from './PageComponent';

interface StateToProps {
  children?: React.ReactNode;
  isSelectionPage: boolean;
  isSideMenuOpen: boolean;
  selection: SelectionState;
}

const PageContainerComponent = (props: StateToProps) => {
  const {
    children,
    selection,
    isSelectionPage,
    isSideMenuOpen,
  } = props;

  const renderSelectionSearch = isSelectionPage
    ? <SelectionMenuContainer/>
    : <SelectionMenuSummary selection={selection}/>;

  return (
    <PageComponent isSideMenuOpen={isSideMenuOpen} renderTopMenuSearch={renderSelectionSearch}>
      {children}
    </PageComponent>
  );
};

const mapStateToProps = ({routing, ui, searchParameters}: RootState): StateToProps => {
  return {
    selection: getSelection(searchParameters),
    isSelectionPage: isSelectionPage(routing),
    isSideMenuOpen: isSideMenuOpen(ui),
  };
};

export const MvpPageContainer =
  connect<StateToProps>(mapStateToProps)(PageContainerComponent);
