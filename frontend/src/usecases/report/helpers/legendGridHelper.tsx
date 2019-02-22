import {GridCellProps, GridColumn, GridRowProps} from '@progress/kendo-react-grid';
import {default as classNames} from 'classnames';
import {noop, toArray} from 'lodash';
import Checkbox from 'material-ui/Checkbox';
import * as React from 'react';
import {IconRightArrow} from '../../../components/icons/IconRightArrow';
import {RowMiddle} from '../../../components/layouts/row/Row';
import {Medium as MediumText} from '../../../components/texts/Texts';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {allQuantities, Medium, Quantity, toMediumText} from '../../../state/ui/graph/measurement/measurementModels';
import {Dictionary, OnClick} from '../../../types/Types';
import {LegendItem} from '../reportModels';
import {isGroupHeader} from './measurementGridHelper';

const renderQuantityCell = (quantity: Quantity) =>
  ({dataItem: {id, label, medium}, columnIndex}: GridCellProps) => {
    const mediumQuantities = allQuantities[medium];
    if (mediumQuantities.some((q) => q === quantity)) {
      const checked = columnIndex && columnIndex === 2 || columnIndex === 3 || mediumQuantities.length < 3;
      return (
        <td>
          <Checkbox checked={checked} onCheck={noop}/>
        </td>);
    } else {
      return <td/>;
    }
  };

const groupHeaderTds = (medium: Medium, columnQuantities: Quantity[]) => {
  const tds = columnQuantities.map((quantity, index) => {
    const key = `group-header-td-${medium}-${quantity}`;
    const mediumQuantities = allQuantities[medium];
    if (mediumQuantities.some((q) => q === quantity)) {
      return (
        <td key={key} className="check-box-td">
          <Checkbox checked={index < 2 || mediumQuantities.length < 3} onCheck={noop}/>
        </td>
      );
    } else {
      return <td key={key}/>;
    }
  });
  tds.push(<td key={`group-header-last-td-${medium}`}/>);
  return tds;
};

const renderGroupHeader = (onExpandRow: OnClick, dataItem: any, columnQuantities: Quantity[]) => {
  const medium: Medium = dataItem.value;
  const mediumText = toMediumText(medium).toLowerCase();
  const onClick = () => onExpandRow(dataItem);
  const isVisible = !!dataItem.expanded;
  return (
    <tr className="GroupHeader Foldable">
      <td colSpan={2}>
        <RowMiddle onClick={onClick} className="clickable">
          <IconRightArrow className={classNames('Foldable-arrow', {isVisible})}/>
          <MediumText className="Bold">{firstUpperTranslated(mediumText)}</MediumText>
        </RowMiddle>
      </td>
      {groupHeaderTds(medium, columnQuantities)}
    </tr>
  );
};

type TableRow = React.ReactElement<HTMLTableRowElement>;

const renderDataRow = (tr: TableRow, dataItem: any) => {
  const {expanded, last} = dataItem;
  return expanded
    ? last ? React.cloneElement(tr, {className: 'last'}, tr.props.children) : tr
    : null;
};

export const rowRenderer = (onExpandRow: OnClick, columnQuantities: Quantity[]) =>
  (tr: TableRow, {dataItem, rowType}: GridRowProps) => {
    if (isGroupHeader(rowType)) {
      return renderGroupHeader(onExpandRow, dataItem, columnQuantities);
    } else {
      return renderDataRow(tr, dataItem);
    }
  };

export const quantityWidth = 90;

export const renderColumns =
  (legendItems: LegendItem[]): [React.ReactNode[], Quantity[]] => {
    const columns: Dictionary<React.ReactNode> = {};
    const columnQuantities: Quantity[] = [];

    legendItems.forEach(({id, label, medium}: LegendItem) => {
      allQuantities[medium].forEach((quantity) => {
        if (columns[quantity] === undefined) {
          columns[quantity] = (
            <GridColumn
              key={`legend-${id}-${label}-${medium}-${quantity}`}
              title={`${translate(`${quantity} short`)}`}
              cell={renderQuantityCell(quantity)}
              width={quantityWidth}
            />);
          columnQuantities.push(quantity);
        }
      });
    });

    return [toArray(columns), columnQuantities];
  };
