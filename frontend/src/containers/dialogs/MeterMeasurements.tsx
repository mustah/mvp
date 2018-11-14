import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {withLargeLoader} from '../../components/hoc/withLoaders';
import {Column} from '../../components/layouts/column/Column';
import '../../components/table/Table.scss';
import {Normal} from '../../components/texts/Texts';
import {TimestampInfoMessage} from '../../components/timestamp-info-message/TimestampInfoMessage';
import {isDefined} from '../../helpers/commonUtils';
import {timestamp} from '../../helpers/dateHelpers';
import {roundMeasurement} from '../../helpers/formatters';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {MeterDetails} from '../../state/domain-models/meter-details/meterDetailsModels';
import {fetchMeasurementsPaged} from '../../state/ui/graph/measurement/measurementActions';
import {
  getMediumType,
  initialMeterMeasurementsState,
  Measurement,
  MeterMeasurementsState,
  Quantity,
  Readings
} from '../../state/ui/graph/measurement/measurementModels';
import {Children, Fetching, UnixTimestamp} from '../../types/Types';
import {logout} from '../../usecases/auth/authActions';
import {OnLogout} from '../../usecases/auth/authModels';
import {fillMissingMeasurements, groupMeasurementsByDate, MeasurementTableData} from './dialogHelper';

const renderValue = ({value, unit}: Measurement): string =>
  value !== undefined && unit ? `${roundMeasurement(value)} ${unit}` : '';

const renderCreated = (created: UnixTimestamp): Children =>
  created
    ? timestamp(created * 1000)
    : <Normal className="Italic">{firstUpperTranslated('never collected')}</Normal>;

const renderReadingRows =
  (quantities: Quantity[]) =>
    (readings: Readings): Array<React.ReactElement<HTMLTableRowElement>> => {
      const rows: Array<React.ReactElement<any>> = [];

      const orderedReadingTimestamps: UnixTimestamp[] = Array.from(readings.keys()).sort().reverse();
      orderedReadingTimestamps
        .forEach((timestamp: UnixTimestamp) => {
          const reading = readings.get(timestamp)!;
          const row = reading.measurements
            ? quantities
              .map((quantity: Quantity) => reading.measurements![quantity])
              .filter(isDefined)
              .map((measurement: Measurement, index: number) => <td key={index}>{renderValue(measurement)}</td>)
            : <td key={1}>{translate('measurement')}</td>;

          rows.push((
            <tr key={timestamp}>
              <td key="created">{renderCreated(timestamp)}</td>
              {row}
            </tr>
          ));
        });

      return rows;
    };

const readoutColumnStyle: React.CSSProperties = {
  width: 80,
};

interface ReadingsProps {
  readings: Readings;
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
    <TimestampInfoMessage/>
  </Column>
);

type WrapperProps = Fetching & ReadingsProps;

const MeasurementsTableComponent = withLargeLoader<ReadingsProps>(MeasurementsTable);

type Props = OwnProps & DispatchToProps;

class MeterMeasurements extends React.Component<Props, MeterMeasurementsState> {

  constructor(props) {
    super(props);
    this.state = {...initialMeterMeasurementsState};
  }

  async componentDidMount() {
    const {meter: {id}, logout} = this.props;

    this.setState({isFetching: true});

    await fetchMeasurementsPaged(id, this.updateState, logout);
  }

  async componentWillReceiveProps({meter: {id}, logout}: Props) {
    this.setState({isFetching: true});

    await fetchMeasurementsPaged(id, this.updateState, logout);
  }

  render() {
    const {isFetching, measurementPages} = this.state;
    const {meter: {medium, readIntervalMinutes}} = this.props;

    const {readings, quantities}: MeasurementTableData = groupMeasurementsByDate(
      measurementPages,
      getMediumType(medium),
    );

    const paddedReadings: Readings = fillMissingMeasurements({
      receivedData: readings,
      readIntervalMinutes,
      lastDate: new Date(),
      numberOfRows: 100
    });

    const wrapperProps: WrapperProps = {
      isFetching,
      readings: paddedReadings,
      quantities,
    };

    return <MeasurementsTableComponent {...wrapperProps}/>;
  }

  updateState = (state: MeterMeasurementsState) => this.setState({...state});
}

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  logout,
}, dispatch);

export const MeterMeasurementsContainer = connect<{}, DispatchToProps, OwnProps>(
  mapDispatchToProps,
)(MeterMeasurements);
