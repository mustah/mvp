import {GridCellProps, GridHeaderCellProps, GridRowProps} from '@progress/kendo-react-grid';
import {GridRowType} from '@progress/kendo-react-grid/dist/es/interfaces/GridRowType';
import * as React from 'react';
import {borderStyle, colors} from '../../../app/themes';
import {firstUpperTranslated} from '../../../services/translationService';
import {toMediumText} from '../../../state/ui/graph/measurement/measurementModels';
import {RowMiddle} from '../../../components/layouts/row/Row';
import {InfoText, Medium as MediumText} from '../../../components/texts/Texts';

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
