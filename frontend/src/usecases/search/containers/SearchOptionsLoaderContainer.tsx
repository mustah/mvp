import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {RowCenter} from '../../common/components/layouts/row/Row';
import {Bold} from '../../common/components/texts/Texts';
import {fetchSearchOptions} from '../searchActions';
import {isFetching} from '../searchSelectors';

interface OwnProps {
  children: React.ReactElement<any> | null;
}

interface StateToProps {
  isFetching: boolean;
}

interface DispatchToProps {
  fetchSearchOptions: () => void;
}

type Props = StateToProps & DispatchToProps & OwnProps;

class SearchOptionsLoaderContainerComponent extends React.Component<Props> {

  componentWillMount() {
    this.props.fetchSearchOptions();
  }

  render() {
    const {isFetching} = this.props;
    if (isFetching) {
      return <RowCenter><Bold>...Söker</Bold></RowCenter>;
    } else {
      return this.props.children;
    }
  }
}

const mapStateToProps = ({search}: RootState): StateToProps => {
  return {
    isFetching: isFetching(search),
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchSearchOptions,
}, dispatch);

export const SearchOptionsLoaderContainer =
  connect<StateToProps, DispatchToProps, {}>
  (mapStateToProps, mapDispatchToProps)(SearchOptionsLoaderContainerComponent);
