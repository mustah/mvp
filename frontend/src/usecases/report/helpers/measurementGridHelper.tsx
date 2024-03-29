import {GridCellProps, GridColumnProps, GridHeaderCellProps, GridRowProps} from '@progress/kendo-react-grid';
import {GridRowType} from '@progress/kendo-react-grid/dist/es/interfaces/GridRowType';
import {toArray} from 'lodash';
import * as React from 'react';
import {colors} from '../../../app/colors';
import {dividerBorder} from '../../../app/themes';
import {RowMiddle} from '../../../components/layouts/row/Row';
import {InfoText, Medium as MediumText} from '../../../components/texts/Texts';
import {displayDate} from '../../../helpers/dateHelpers';
import {roundMeasurement} from '../../../helpers/formatters';
import {capitalized, firstUpperTranslated, translate} from '../../../services/translationService';
import {LegendTyped} from '../../../state/report/reportModels';
import {
  getGroupHeaderTitle,
  getMediumType,
  MeasurementResponsePart,
  MeasurementsApiResponse,
  Quantity,
  quantityComparator,
  weightedQuantity
} from '../../../state/ui/graph/measurement/measurementModels';
import {Dictionary} from '../../../types/Types';

export const isGroupHeader = (rowType?: GridRowType): boolean => rowType === 'groupHeader';

export const headerCellRender = (th, {field}: GridHeaderCellProps) => {
  if (field === 'value') {
    const style: React.CSSProperties = {background: colors.white, height: 34};
    return React.cloneElement(th, {style});
  } else {
    return th;
  }
};

export const cellRender = (td, {columnIndex, rowType}: GridCellProps) => {
  if (columnIndex === 0) {
    const cellStyle: React.CSSProperties = isGroupHeader(rowType)
      ? {
        background: colors.alternate,
        borderBottom: dividerBorder,
        borderTop: dividerBorder,
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
    const mediumText = dataItem.items.length && dataItem.items[0].type;
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

interface ListItem extends LegendTyped {
  label: string;
  meterId?: string;
  name?: string;
  values: Dictionary<string>;
  when: string;
}

type ColumnProps = GridColumnProps & {quantity: Quantity};

export const renderColumns =
  (measurements: MeasurementsApiResponse): [ListItem[], GridColumnProps[]] => {
    const rows: Dictionary<ListItem> = {};
    const columns: Dictionary<ColumnProps> = {};

    measurements.forEach(({
      name,
      label,
      medium,
      meterId,
      unit,
      values,
      quantity,
    }: MeasurementResponsePart) => {
      const legendType = medium ? getMediumType(medium) : 'aggregate';
      const formattedName = medium ? name : firstUpperTranslated('average');
      const type = medium ? getGroupHeaderTitle(legendType) : label;

      values.forEach(({when, value}) => {
        const rowKey = `${when}-${label}`;
        const listItem: ListItem = rows[rowKey] || {
          label,
          name: formattedName,
          meterId,
          type,
          when: displayDate(when * 1000),
          values: {},
        };
        listItem.values[quantity] = value !== undefined ? roundMeasurement(value) : '-';
        rows[rowKey] = listItem;
      });

      if (columns[quantity] === undefined) {
        columns[quantity] = {
          field: `values.${quantity}`,
          headerClassName: 'quantity',
          orderIndex: weightedQuantity[quantity],
          quantity,
          title: `${capitalized(translate(`${quantity} short`))} (${unit})`
        };
      }
    });

    return [toArray(rows).reverse(), toArray(columns).sort((a, b) => quantityComparator(a.quantity, b.quantity))];
  };
