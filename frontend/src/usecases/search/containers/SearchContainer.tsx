import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {RootState} from '../../../reducers/index';
import {PageContainer} from '../../common/components/layouts/layout/PageLayout';
import {SearchContentBox} from '../components/SearchContentBox';
import {SearchParameter} from '../models/searchParameterModels';
import {selectSearchOption} from '../searchActions';
import {SearchParametersState} from '../searchReducer';

export interface SearchStateToProps {
  searchParameters: SearchParametersState;
}

export interface SearchDispatchToProps {
  selectSearchOption: (searchParameters: SearchParameter) => void;
}

export const SearchContainerComponent = (props: SearchStateToProps & SearchDispatchToProps & InjectedAuthRouterProps) =>
  (
    <PageContainer>
      <SearchContentBox
        searchParameters={props.searchParameters}
        selectSearchOption={props.selectSearchOption}
      />
    </PageContainer>
  );

const mapStateToProps = (state: RootState): SearchStateToProps => {
  const {searchParameters} = state;
  return {
    searchParameters,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  selectSearchOption,
}, dispatch);

export const SearchContainer =
  connect<SearchStateToProps, SearchDispatchToProps, {}>
  (mapStateToProps, mapDispatchToProps)(SearchContainerComponent);
