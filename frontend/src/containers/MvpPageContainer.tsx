import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../reducers/rootReducer';
import {isSelectionPage} from '../selectors/routerSelectors';
import {isSideMenuOpen} from '../state/ui/uiSelectors';
import {resetSelection, selectSavedSelection} from '../state/user-selection/userSelectionActions';
import {UserSelection} from '../state/user-selection/userSelectionModels';
import {getUserSelection} from '../state/user-selection/userSelectionSelectors';
import {OnClick, OnClickWithId} from '../types/Types';
import {SelectionMenuSummary} from '../usecases/selection/components/selection-menu/SelectionMenuSummary';
import {SelectionMenuContainer} from '../usecases/selection/containers/SelectionMenuContainer';
import {PageComponent} from './PageComponent';

interface StateToProps {
  children?: React.ReactNode;
  isSelectionPage: boolean;
  isSideMenuOpen: boolean;
  selection: UserSelection;
}

interface DispatchToProps {
  selectSavedSelection: OnClickWithId;
  resetSelection: OnClick;
}

type Props = StateToProps & DispatchToProps;

const MvpPageComponent = ({
  children,
  selection,
  isSelectionPage,
  isSideMenuOpen,
  selectSavedSelection,
  resetSelection,
}: Props) => {

  const renderSelectionSearch = isSelectionPage
    ? <SelectionMenuContainer/>
    : (
      <SelectionMenuSummary
        selection={selection}
        selectSavedSelection={selectSavedSelection}
        resetSelection={resetSelection}
      />
    );

  return (
    <PageComponent isSideMenuOpen={isSideMenuOpen} renderTopMenuSearch={renderSelectionSearch}>
      {children}
    </PageComponent>
  );
};

const mapStateToProps = ({routing, ui, userSelection}: RootState): StateToProps => ({
  selection: getUserSelection(userSelection),
  isSelectionPage: isSelectionPage(routing),
  isSideMenuOpen: isSideMenuOpen(ui),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  resetSelection,
  selectSavedSelection,
}, dispatch);

export const MvpPageContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(MvpPageComponent);
