import FlatButton from 'material-ui/FlatButton';
import NavigationChevronLeft from 'material-ui/svg-icons/navigation/chevron-left';
import NavigationChevronRight from 'material-ui/svg-icons/navigation/chevron-right';
import * as React from 'react';
import {idGenerator} from '../../helpers/idGenerator';
import {Pagination} from '../../state/ui/pagination/paginationModels';
import {uuid} from '../../types/Types';
import {RowCenter} from '../layouts/row/Row';
import {PageNumberButton} from './PageNumberButton';
import './PaginationControl.scss';

type PageElements = Array<React.ReactElement<FlatButton | HTMLSpanElement>>;

interface PageNumberProps {
  current: number;
  total: number;
  changePage: (page: number) => void;
}

const visibilityProximity: number = 5;

const renderPageNumberButtons = ({total, current, changePage}: PageNumberProps): PageElements => {
  const pages: PageElements = [];
  const paginationUuid: uuid = idGenerator.uuid();

  let lastPrintedAreDots = false;

  for (let page = 1; page <= total; page++) {
    const key = `pagination-${page}-${paginationUuid}`;
    if (page === current + 1) {
      pages.push(<PageNumberButton disabled={true} key={key} page={page}/>);
      lastPrintedAreDots = false;
    } else if (Math.abs(page - current + 1) <= visibilityProximity
      || page <= visibilityProximity
      || page > (total - visibilityProximity)) {
      const onClick = () => changePage(page - 1);
      pages.push(<PageNumberButton onClick={onClick} key={key} page={page}/>);
      lastPrintedAreDots = false;
    } else if (!lastPrintedAreDots) {
      pages.push(<span key={key}>...</span>);
      lastPrintedAreDots = true;
    }
  }
  return pages;
};

const iconArrowStyle = {marginTop: 8};

interface Props {
  pagination: Pagination;
  changePage: (page: number) => void;
}

export const PaginationControl =
  ({pagination: {page, size, totalPages}, changePage}: Props) => {

    if (totalPages <= 1) {
      return null;
    }

    const noPrev = page === 0;
    const noNext = page + 1 >= totalPages;

    const changePagePrev = noPrev ? () => void(0) : () => changePage(page - 1);
    const changePageNext = noNext ? () => void(0) : () => changePage(page + 1);

    const pageNumberButtons = renderPageNumberButtons({
      current: page,
      total: totalPages,
      changePage,
    });

    return (
      <RowCenter>
        <RowCenter className="PaginationControl">
          <FlatButton disabled={noPrev} onClick={changePagePrev} className="PageNumber-arrow">
            <NavigationChevronLeft style={iconArrowStyle}/>
          </FlatButton>
          {pageNumberButtons}
          <FlatButton disabled={noNext} onClick={changePageNext} className="PageNumber-arrow">
            <NavigationChevronRight style={iconArrowStyle}/>
          </FlatButton>
        </RowCenter>
      </RowCenter>
    );
  };
