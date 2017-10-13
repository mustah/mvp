import {Location} from 'history';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../../../reducers/index';
import {getLocation} from '../../../../../selectors/routerSelector';
import {closeSearch} from '../../../../search/searchActions';
import {SelectionSearch} from '../../selection-search/SelectionSearch';
import {SelectionSearchSummary} from '../../selection-search/SelectionSearchSummary';
import {Column} from '../column/Column';
import {Content} from '../content/Content';
import {Layout} from './Layout';

interface StateToProps {
  children?: React.ReactNode[] | React.ReactNode;
  location: Location;
}

interface DispatchToProps {
  closeSearch: () => void;
}

const isSearchPage = (location: Location): boolean => location.pathname.endsWith('/search');

const PageLayoutContainerComponent = (props: StateToProps & DispatchToProps) => {
  const {children, closeSearch, location} = props;

  const renderSelectionSearch = isSearchPage(location)
    ? <SelectionSearch close={closeSearch}/>
    : <SelectionSearchSummary location={location}/>;

  return (
    <Layout>
      <Column className="flex-1">
        {renderSelectionSearch}
        <Content>
          {children}
        </Content>
      </Column>
    </Layout>
  );
};

const mapStateToProps = (state: RootState) => {
  return {
    location: getLocation(state.routing),
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  closeSearch,
}, dispatch);

export const PageContainer =
  connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(PageLayoutContainerComponent);
