import * as classNames from 'classnames';
import {Pathname} from 'history';
import * as React from 'react';
import {connect} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {getPathname, isSelectionPage} from '../../../selectors/routerSelectors';
import {SelectionState} from '../../../state/search/selection/selectionModels';
import {getSelection} from '../../../state/search/selection/selectionSelectors';
import {isSideMenuOpen} from '../../../state/ui/uiSelectors';
import {SelectionMenuSummary} from '../../selection/components/selection-menu/SelectionMenuSummary';
import {SearchMenuWrapper} from '../../selection/components/selection-menu/SelectionMenuWrapper';
import {SelectionMenuContainer} from '../../selection/containers/SelectionMenuContainer';
import {Column} from '../components/layouts/column/Column';
import {Layout} from '../components/layouts/layout/Layout';

interface StateToProps {
  children?: React.ReactNode;
  isSelectionPage: boolean;
  isSideMenuOpen: boolean;
  pathname: Pathname;
  selection: SelectionState;
}

const PageContainerComponent = (props: StateToProps) => {
  const {
    children,
    selection,
    isSelectionPage,
    isSideMenuOpen,
    pathname,
  } = props;

  const renderSelectionSearch = isSelectionPage
    ? <SelectionMenuContainer/>
    : <SelectionMenuSummary pathname={pathname} selection={selection}/>;

  return (
    <Layout>
      <SearchMenuWrapper className={classNames({isSideMenuOpen})}>
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
    isSelectionPage: isSelectionPage(routing),
    pathname: getPathname(routing),
    isSideMenuOpen: isSideMenuOpen(ui),
  };
};

export const PageContainer =
  connect<StateToProps, {}, {}>(mapStateToProps)(PageContainerComponent);
