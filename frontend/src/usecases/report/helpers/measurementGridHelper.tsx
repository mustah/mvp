import {GridCellProps, GridColumn, GridHeaderCellProps, GridRowProps} from '@progress/kendo-react-grid';
import {GridRowType} from '@progress/kendo-react-grid/dist/es/interfaces/GridRowType';
import {toArray} from 'lodash';
import * as React from 'react';
import {borderStyle, colors} from '../../../app/themes';
import {RowMiddle} from '../../../components/layouts/row/Row';
import {InfoText, Medium as MediumText} from '../../../components/texts/Texts';
import {displayDate} from '../../../helpers/dateHelpers';
import {roundMeasurement} from '../../../helpers/formatters';
import {translate} from '../../../services/translationService';
import {
  getGroupHeaderTitle,
  getMediumType,
  MeasurementResponsePart,
  MeasurementsApiResponse
} from '../../../state/ui/graph/measurement/measurementModels';
import {Dictionary} from '../../../types/Types';
import {LegendType} from '../reportModels';

export const isGroupHeader = (rowType?: GridRowType): boolean => rowType === 'groupHeader';

export const headerCellRender = (th, {field}: GridHeaderCellProps) => {
  if (field === 'value') {
    const style: React.CSSProperties = {background: colors.white, height: 34};
    return React.cloneElement(th, {style});
  } else {
    return th;
  }
};

export const cellRender = (td, {columnIndex, dataItem, rowType}: GridCellProps) => {
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

export const rowRender = (tr: React.ReactElement<HTMLTableRowElement>, {dataItem, rowType}: GridRowProps) => {
  if (isGroupHeader(rowType)) {
    const mediumText = getGroupHeaderTitle(dataItem.items.length && dataItem.items[0].type);
    return (
      <tr className="GroupHeader">
        <td colSpan={14}>
          <RowMiddle>
            <MediumText className="Bold">{dataItem.value}</MediumText>
            <InfoText style={{marginLeft: 16}}>{mediumText}</InfoText>
          </RowMiddle>
        </td>
      </tr>
    );
  } else {
    return tr;
  }
};

interface ListItem {
  when: string;
  label: string;
  values: Dictionary<string>;
  type: LegendType;
}

export const renderColumns = (measurements: MeasurementsApiResponse): [ListItem[], React.ReactNode[]] => {
  const rows: Dictionary<ListItem> = {};
  const columns: Dictionary<React.ReactNode> = {};

  measurements.forEach(({id, label, medium: type, unit, values, quantity}: MeasurementResponsePart) => {
    if (columns[quantity] === undefined) {
      const title = `${translate(`${quantity} short`)} (${unit})`;
      columns[quantity] = <GridColumn key={`${id}-${title}`} title={title} field={`values.${quantity}`}/>;
    }

    values.forEach(({when, value}) => {
      const rowKey = `${when}-${label}`;
      const legendType = type ? getMediumType(type) : 'aggregate';
      const listItem: ListItem = rows[rowKey] || {
        label,
        type: legendType,
        when: displayDate(when * 1000),
        values: {},
      };
      listItem.values[quantity] = value !== undefined ? roundMeasurement(value) : '-';
      rows[rowKey] = listItem;
    });
  });

  return [toArray(rows).reverse(), toArray(columns)];
};
