import {DataResult, process, State} from '@progress/kendo-data-query';
import {ExcelExport} from '@progress/kendo-react-excel-export';
import {Grid, GridCellProps, GridColumn, GridHeaderCellProps, GridRowProps} from '@progress/kendo-react-grid';
import {GridRowType} from '@progress/kendo-react-grid/dist/es/interfaces/GridRowType';
import {toArray} from 'lodash';
import * as React from 'react';
import {borderStyle, colors} from '../../../app/themes';
import {Column} from '../../../components/layouts/column/Column';
import {RowMiddle} from '../../../components/layouts/row/Row';
import {InfoText, Medium as MediumText} from '../../../components/texts/Texts';
import {TimestampInfoMessage} from '../../../components/timestamp-info-message/TimestampInfoMessage';
import {timestamp} from '../../../helpers/dateHelpers';
import {roundMeasurement} from '../../../helpers/formatters';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {
  getMediumType,
  MeasurementApiResponse,
  MeasurementResponsePart,
  Measurements,
  Medium,
  Quantity,
  toMediumText,
} from '../../../state/ui/graph/measurement/measurementModels';
import {Callback, Dictionary} from '../../../types/Types';

export interface MeasurementListProps extends Measurements {
  isExportingToExcel: boolean;
  exportToExcelSuccess: Callback;
}

interface MeasurementListItem {
  when: string;
  label: string;
  values: { [key in Quantity]: string };
  medium: Medium;
}

const makeMeasurementListItems = (measurements: MeasurementApiResponse): [MeasurementListItem[], React.ReactNode[]] => {
  const rows: Dictionary<MeasurementListItem> = {};
  const columns: Dictionary<React.ReactNode> = {};

  measurements.forEach(({id, label, medium, unit, values, quantity}: MeasurementResponsePart) => {
    if (columns[quantity] === undefined) {
      const key = `${translate(`${quantity} short`)} (${unit})`;
      columns[quantity] = (
        <GridColumn
          key={key}
          title={key}
          field={`values.${quantity}`}
        />
      );
    }

    values.forEach(({when, value}) => {
      const rowIndex = when + id;
      const listItem: MeasurementListItem = rows[rowIndex] || {
        label,
        medium: getMediumType(medium),
        when: timestamp(when * 1000),
        values: {},
      };
      listItem.values[quantity] = value !== undefined ? roundMeasurement(value) : '-';
      rows[rowIndex] = listItem;
    });
  });

  return [toArray(rows).reverse(), toArray(columns)];
};

const isGroupHeader = (rowType?: GridRowType): boolean => rowType === 'groupHeader';

const headerCellRender = (th, {field}: GridHeaderCellProps) => {
  if (field === 'value') {
    const style: React.CSSProperties = {background: colors.white, height: 34};
    return React.cloneElement(th, {style});
  } else {
    return th;
  }
};

const cellRender = (td, {columnIndex, dataItem, rowType}: GridCellProps) => {
  if (columnIndex === 0) {
    const cellStyle: React.CSSProperties = isGroupHeader(rowType)
      ? {
        background: colors.lightGrey,
        borderBottom: borderStyle,
        borderTop: borderStyle,
        paddingTop: 24
      }
      : {background: colors.white, width: 0, paddingLeft: 0};
    const style: React.CSSProperties = {height: 28, ...cellStyle};
    return React.cloneElement(td, {style}, td.props.children);
  } else {
    return td;
  }
};

const rowRender = (tr, {dataItem, rowType}: GridRowProps) => {
  if (isGroupHeader(rowType)) {
    const mediumText = toMediumText(dataItem.items.length && dataItem.items[0].medium);
    return (
      <tr className="GroupHeader">
        <td colSpan={14}>
          <RowMiddle>
            <MediumText className="Bold">{dataItem.value}</MediumText>
            <InfoText style={{marginLeft: 16}}>{firstUpperTranslated(mediumText.toLowerCase())}</InfoText>
          </RowMiddle>
        </td>
      </tr>
    );
  } else {
    return tr;
  }
};

export const MeasurementList = ({measurements, exportToExcelSuccess, isExportingToExcel}: MeasurementListProps) => {
  const [listItems, quantityColumns] = React.useMemo(() => makeMeasurementListItems(measurements), [measurements]);

  const exporter = React.useRef();

  React.useEffect(() => {
    if (isExportingToExcel) {
      // TODO[!must!]: Our types for React's hooks are wrong. It is solved in the newest version of react.
      (exporter as any).current.save();
      exportToExcelSuccess();
    }
  }, [isExportingToExcel]);

  const columns = [
    (
      <GridColumn
        headerClassName="left-most"
        className="left-most"
        field="when"
        key="when"
        title={firstUpperTranslated('readout')}
      />
    ),
    ...quantityColumns,
  ];

  const state: State = {group: [{field: 'label'}]};
  const dataResult: DataResult = process(listItems, state);

  return (
    <Column className="Grouping-grid">
      <ExcelExport data={dataResult.data} ref={exporter} {...state}>
        <Grid
          scrollable="none"
          data={dataResult}
          groupable={true}
          cellRender={cellRender}
          headerCellRender={headerCellRender}
          rowRender={rowRender}
          {...state}
        >
          {columns}
        </Grid>
      </ExcelExport>
      <TimestampInfoMessage/>
    </Column>
  );
};
