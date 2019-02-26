import {GridCellProps, GridColumn, GridRowProps} from '@progress/kendo-react-grid';
import {default as classNames} from 'classnames';
import {noop, toArray} from 'lodash';
import Checkbox from 'material-ui/Checkbox';
import * as React from 'react';
import {ButtonDelete} from '../../../components/buttons/ButtonDelete';
import {ButtonVisibility} from '../../../components/buttons/ButtonVisibility';
import {IconRightArrow} from '../../../components/icons/IconRightArrow';
import {RowLeft, RowMiddle, RowRight} from '../../../components/layouts/row/Row';
import {Medium as MediumText} from '../../../components/texts/Texts';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {colorizeMeters} from '../../../state/ui/graph/measurement/graphContentsMapper';
import {allQuantities, Medium, Quantity, toMediumText} from '../../../state/ui/graph/measurement/measurementModels';
import {Dictionary, OnClick, OnClickWith} from '../../../types/Types';
import {LegendItem, MediumViewOptions} from '../reportModels';
import {isGroupHeader} from './measurementGridHelper';

export interface RowProps {
  onExpandRow: OnClick;
  columnQuantities: Quantity[];
  hideAllByMedium: OnClickWith<Medium>;
  mediumViewOptions: MediumViewOptions;
  removeAllByMedium: OnClickWith<Medium>;
}

interface MediumTdProps {
  removeAll: OnClick;
  hideAll: OnClick;
  isAllHidden: boolean;
}

const MediumTd = ({hideAll, isAllHidden, removeAll}: MediumTdProps) => (
  <td className="check-box-td">
    <RowLeft>
      <RowRight title={firstUpperTranslated('hide all')}>
        <ButtonVisibility onClick={hideAll} checked={isAllHidden}/>
      </RowRight>
      <RowRight title={firstUpperTranslated('remove all')}>
        <ButtonDelete onClick={removeAll}/>
      </RowRight>
    </RowLeft>
  </td>
);

const renderGroupHeaderTds = (props: RowProps, medium: Medium) => {
  const {columnQuantities, hideAllByMedium, mediumViewOptions, removeAllByMedium} = props;
  const tds = columnQuantities.map((quantity, index) => {
    const key = `group-header-td-${medium}-${quantity}`;
    const mediumQuantities = allQuantities[medium];
    if (mediumQuantities.some((q) => q === quantity)) {
      const checked = index < 2 || mediumQuantities.length === 1;
      return (
        <td key={key} className="check-box-td">
          <Checkbox checked={checked} onCheck={noop} iconStyle={{fill: colorizeMeters(quantity)}}/>
        </td>
      );
    } else {
      return <td key={key}/>;
    }
  });
  const removeAll = () => removeAllByMedium(medium);
  const hideAll = () => hideAllByMedium(medium);
  tds.push(
    <MediumTd
      key={`group-header-last-td-${medium}`}
      hideAll={hideAll}
      isAllHidden={!(!mediumViewOptions[medium].isAllLinesHidden)}
      removeAll={removeAll}
    />
  );
  return tds;
};

const renderGroupHeader = (props: RowProps, dataItem: any) => {
  const medium: Medium = dataItem.value;
  const mediumText = toMediumText(medium).toLowerCase();
  const isVisible = dataItem.items[0].isRowExpanded;
  const onClick = () => props.onExpandRow(dataItem);
  return (
    <tr className="GroupHeader Foldable" key={`group-header-${medium}`}>
      <td colSpan={2}>
        <RowMiddle onClick={onClick} className="clickable">
          <IconRightArrow className={classNames('Foldable-arrow', {isVisible})}/>
          <MediumText className="Bold">{firstUpperTranslated(mediumText)}</MediumText>
        </RowMiddle>
      </td>
      {renderGroupHeaderTds(props, medium)}
    </tr>
  );
};

type TableRow = React.ReactElement<HTMLTableRowElement>;

const renderDataRow = (tr: TableRow, dataItem: any) => {
  const {isRowExpanded, last} = dataItem;
  return isRowExpanded
    ? last ? React.cloneElement(tr, {className: 'last'}, tr.props.children) : tr
    : null;
};

export const rowRenderer = (props: RowProps) =>
  (tr: TableRow, {dataItem, rowType}: GridRowProps) => {
    if (isGroupHeader(rowType)) {
      return renderGroupHeader(props, dataItem);
    } else {
      return renderDataRow(tr, dataItem);
    }
  };

const renderQuantityCell = (quantity: Quantity) =>
  ({dataItem: {id, label, medium}, columnIndex}: GridCellProps) => {
    const mediumQuantities = allQuantities[medium];
    if (mediumQuantities.some((q) => q === quantity)) {
      const checked = columnIndex && columnIndex === 2 || mediumQuantities.length < 2;
      return (
        <td>
          <Checkbox checked={checked} onCheck={noop} iconStyle={{fill: colorizeMeters(quantity)}}/>
        </td>);
    } else {
      return <td/>;
    }
  };

export const quantityColumnWidth = 76;

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
              width={quantityColumnWidth}
            />);
          columnQuantities.push(quantity);
        }
      });
    });

    return [toArray(columns), columnQuantities];
  };
