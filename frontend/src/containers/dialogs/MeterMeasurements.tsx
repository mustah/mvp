import * as React from 'react';
import {connect} from 'react-redux';
import {compose} from 'recompose';
import {bindActionCreators} from 'redux';
import {withEmptyContent, WithEmptyContentProps} from '../../components/hoc/withEmptyContent';
import {withLargeLoader} from '../../components/hoc/withLoaders';
import {Column} from '../../components/layouts/column/Column';
import '../../components/table/Table.scss';
import {TableInfoText} from '../../components/table/TableInfoText';
import {Normal} from '../../components/texts/Texts';
import {timestamp} from '../../helpers/dateHelpers';
import {roundMeasurement} from '../../helpers/formatters';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {MeterDetails} from '../../state/domain-models/meter-details/meterDetailsModels';
import {fetchMeasurementsPaged, groupMeasurementsByDate} from '../../state/ui/graph/measurement/measurementActions';
import {
  initialMeterMeasurementsState,
  Measurement,
  MeterMeasurementsState,
  Reading,
} from '../../state/ui/graph/measurement/measurementModels';
import {Children, Fetching} from '../../types/Types';
import {logout} from '../../usecases/auth/authActions';
import {OnLogout} from '../../usecases/auth/authModels';

const renderQuantityHeader = ({quantity}: Measurement): string => translate(quantity + ' short');

const renderValue = ({value = null, unit}: Measurement): string =>
  value !== null && unit ? `${roundMeasurement(value)} ${unit}` : '';

const renderCreated = (created: number): Children =>
  created
    ? timestamp(created * 1000)
    : <Normal className="Italic">{firstUpperTranslated('never collected')}</Normal>;

const renderMeasurements = (
  id: number,
  measurements: Measurement[],
): Array<React.ReactElement<HTMLTableCellElement>> => {
  const cols: Array<React.ReactElement<HTMLTableCellElement>> = [];

  cols.push(<td key={`${id}-created`}>{renderCreated(id)}</td>);

  measurements.forEach((measurement: Measurement) => {
    cols.push(<td key={id + measurement.quantity}>{renderValue(measurement)}</td>);
  });

  return cols;
};

const renderReadingRows = (readings: Map<number, Reading>): Array<React.ReactElement<HTMLTableRowElement>> => {
  const rows: Array<React.ReactElement<any>> = [];

  readings.forEach((reading: Reading, id: number) => {
    const row: React.ReactElement<HTMLTableRowElement> = (
      <tr key={id}>{renderMeasurements(id, reading.measurements)}</tr>
    );
    rows.push(row);
  });

  return rows;
};

const style: React.CSSProperties = {
  width: 80,
};

const renderHeaders = (measurements: Measurement[]): Array<React.ReactElement<HTMLTableHeaderCellElement>> => {
  const cols: Array<React.ReactElement<HTMLTableHeaderCellElement>> = [];

  cols.push(<th style={style} className="first" key="readout">{translate('readout')}</th>);

  measurements.forEach((measurement: Measurement) => {
    cols.push(<th key={measurement.quantity}>{renderQuantityHeader(measurement)}</th>);
  });

  return cols;
};

interface ReadingsProps {
  readings: Map<number, Reading>;
}

interface OwnProps {
  meter: MeterDetails;
}

interface DispatchToProps {
  logout: OnLogout;
}

const MeasurementsTable = ({readings}: ReadingsProps) => (
  <Column>
    <table key="1" className="Table" cellPadding="0" cellSpacing="0">
      <thead>
      <tr>
        {renderHeaders(readings.values().next().value.measurements)}
      </tr>
      </thead>
      <tbody>
      {renderReadingRows(readings)}
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

    const readings: Map<number, Reading> = groupMeasurementsByDate(measurementPages);

    const wrapperProps: WrapperProps = {
      isFetching,
      hasContent: readings.size > 0,
      noContentText: firstUpperTranslated('measurement', {count: 2}),
      readings,
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
