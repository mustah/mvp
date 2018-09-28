import * as React from 'react';
import {connect} from 'react-redux';
import {compose} from 'recompose';
import {bindActionCreators} from 'redux';
import {withEmptyContent, WithEmptyContentProps} from '../../components/hoc/withEmptyContent';
import {withLargeLoader} from '../../components/hoc/withLoaders';
import {getMediumType} from '../../components/indicators/indicatorWidgetModels';
import {Column} from '../../components/layouts/column/Column';
import '../../components/table/Table.scss';
import {TableInfoText} from '../../components/table/TableInfoText';
import {Normal} from '../../components/texts/Texts';
import {timestamp} from '../../helpers/dateHelpers';
import {roundMeasurement} from '../../helpers/formatters';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {MeterDetails} from '../../state/domain-models/meter-details/meterDetailsModels';
import {fetchMeasurementsPaged} from '../../state/ui/graph/measurement/measurementActions';
import {
  initialMeterMeasurementsState,
  Measurement,
  MeterMeasurementsState,
  Quantity,
  Reading,
} from '../../state/ui/graph/measurement/measurementModels';
import {Children, Fetching} from '../../types/Types';
import {logout} from '../../usecases/auth/authActions';
import {OnLogout} from '../../usecases/auth/authModels';
import {groupMeasurementsByDate, MeasurementTableData} from './dialogHelper';

const renderValue = ({value, unit}: Measurement): string =>
  value !== undefined && unit ? `${roundMeasurement(value)} ${unit}` : '';

const renderCreated = (created: number): Children =>
  created
    ? timestamp(created * 1000)
    : <Normal className="Italic">{firstUpperTranslated('never collected')}</Normal>;

const renderReadingRows =
  (quantities: Quantity[]) =>
    (readings: Map<number, Reading>): Array<React.ReactElement<HTMLTableRowElement>> => {
      const rows: Array<React.ReactElement<any>> = [];

      readings.forEach((reading: Reading, timestamp: number) => {
        const measurements = quantities
          .map((quantity: Quantity) => reading.measurements[quantity]!)
          .map((measurement: Measurement, index: number) =>
            <td key={index}>{renderValue(measurement)}</td>);

        rows.push((
          <tr key={timestamp}>
            <td key="created">{renderCreated(timestamp)}</td>
            {measurements}
          </tr>
        ));
      });

      return rows;
    };

const readoutColumnStyle: React.CSSProperties = {
  width: 80,
};

interface ReadingsProps {
  readings: Map<number, Reading>;
  quantities: Quantity[];
}

interface OwnProps {
  meter: MeterDetails;
}

interface DispatchToProps {
  logout: OnLogout;
}

const renderQuantity =
  (quantity: Quantity) =>
    <th key={quantity}>{translate(quantity + ' short')}</th>;

const MeasurementsTable = ({readings, quantities}: ReadingsProps) => (
  <Column>
    <table key="1" className="Table" cellPadding="0" cellSpacing="0">
      <thead>
      <tr>
        <th style={readoutColumnStyle} className="first" key="readout">{translate('readout')}</th>
        {quantities.map(renderQuantity)}
      </tr>
      </thead>
      <tbody>
      {renderReadingRows(quantities)(readings)}
      </tbody>
    </table>
    <TableInfoText/>
  </Column>
);

type WrapperProps = Fetching & WithEmptyContentProps & ReadingsProps;

const enhance = compose<ReadingsProps, WrapperProps>(withLargeLoader, withEmptyContent);

const MeasurementsTableComponent = enhance(MeasurementsTable);

type Props = OwnProps & DispatchToProps;

class MeterMeasurements extends React.Component<Props, MeterMeasurementsState> {

  constructor(props) {
    super(props);
    this.state = {...initialMeterMeasurementsState};
  }

  updateState = (state: MeterMeasurementsState) => this.setState({...state});

  async componentDidMount() {
    const {meter, logout} = this.props;

    this.setState({isFetching: true});

    await fetchMeasurementsPaged(meter, this.updateState, logout);
  }

  async componentWillReceiveProps({meter, logout}: Props) {
    this.setState({isFetching: true});

    await fetchMeasurementsPaged(meter, this.updateState, logout);
  }

  render() {
    const {isFetching, measurementPages} = this.state;
    const {meter: {medium}} = this.props;

    const {readings, quantities}: MeasurementTableData = groupMeasurementsByDate(
      measurementPages,
      getMediumType(medium),
    );

    const wrapperProps: WrapperProps = {
      isFetching,
      hasContent: readings.size > 0,
      noContentText: firstUpperTranslated('measurement', {count: 2}),
      readings,
      quantities,
    };

    return <MeasurementsTableComponent {...wrapperProps}/>;
  }
}

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  logout,
}, dispatch);

export const MeterMeasurementsContainer = connect<{}, DispatchToProps, OwnProps>(
  mapDispatchToProps,
)(MeterMeasurements);
