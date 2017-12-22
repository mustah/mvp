import * as React from 'react';
import {connect} from 'react-redux';
import {withRouter} from 'react-router';
import {bindActionCreators} from 'redux';
import {RootState} from '../reducers/rootReducer';
import {fetchGateways, fetchMeters} from '../state/domain-models/domainModelsActions';
import {getEncodedUriParametersForMeters} from '../state/search/selection/selectionSelectors';
import './App.scss';
import {MainPages} from './MainPages';

interface StateToProps {
  encodedUriParametersForMeters: string;
  encodedUriParametersForGateways: string;
}

interface DispatchToProps {
  fetchGateways: (encodedUriParameters: string) => void;
  fetchMeters: (encodedUriParameters: string) => void;
}

/**
 * The Application root component should extend React.Component in order
 * for HMR (hot module reloading) to work properly. Otherwise, prefer
 * functional components.
 */
class AppComponent extends React.Component<StateToProps & DispatchToProps> {

  componentDidMount() {
    const {fetchGateways, fetchMeters, encodedUriParametersForMeters, encodedUriParametersForGateways} = this.props;
    fetchGateways(encodedUriParametersForMeters);
    fetchMeters(encodedUriParametersForGateways);
  }

  render() {
     return (<MainPages/>);
  }
}

const mapStateToProps = ({searchParameters}: RootState): StateToProps => {
  return {
    encodedUriParametersForMeters: getEncodedUriParametersForMeters(searchParameters),
    encodedUriParametersForGateways: getEncodedUriParametersForMeters(searchParameters),
  };
};

const mapDispatchToProps = (dispatch) => bindActionCreators({
  fetchGateways,
  fetchMeters,
}, dispatch);

const AppContainer = connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(AppComponent);

export const App = withRouter(AppContainer);
