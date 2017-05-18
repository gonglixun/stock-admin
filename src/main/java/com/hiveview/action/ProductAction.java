package com.hiveview.action;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.StringUtil;
import com.hiveview.action.base.BaseController;
import com.hiveview.entity.ApprovalRecord;
import com.hiveview.entity.Paging;
import com.hiveview.entity.Product;
import com.hiveview.service.IApprovalRecordService;
import com.hiveview.service.IProductService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import utils.IssueType;
import utils.log.LogMgr;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/product")
public class ProductAction extends BaseController {

    @Autowired
    private IProductService productService;
    @Autowired
    private IApprovalRecordService approvalRecordService;



    @RequestMapping(value="/list")
    public String list() {
        return "product/product_list";
    }

    /**
     * 去审批
     * @param id
     * @param mav
     * @return
     */
    @RequestMapping(value="/toApproval/{id}")
    public ModelAndView toApproval(@PathVariable("id") long id,ModelAndView mav) {
        Product product = productService.getProductById(id);
        mav.getModel().put("product", product);
        mav.setViewName("product/approval");
        return mav;
    }
    @RequestMapping(value="/page")
    public ModelAndView page(HttpServletRequest request, ModelAndView mav) {
        Paging paging = super.getPaging(request);
        Product product = new Product();
        String status = request.getParameter("status");
        if (StringUtil.isNotEmpty(status)) {
                product.setStatus(Integer.parseInt(status));
        }
        Page<Object> page = PageHelper.startPage(paging.getCurrentPage(), paging.getPageSize());
        List<Product> products =  productService.getProductPage(product);
        paging.setTotalPages(page.getPages());
        mav.getModel().put("paging",paging);
        mav.getModel().put("products",products);
        mav.setViewName("product/paging");
        return mav;
    }

    /**
     * 审批记录列表
     * @param request
     * @param mav
     * @return
     */
    @RequestMapping(value="/approvalPage")
    public ModelAndView approvalPage(HttpServletRequest request, ModelAndView mav) {
        Paging paging = super.getPaging(request);
        String relateId = request.getParameter("relateId");
        if (StringUtils.isNotEmpty(relateId)) {
            ApprovalRecord approvalRecord = new ApprovalRecord();
            approvalRecord.setType(IssueType.PRODUCT.getVal());
            approvalRecord.setRelateId(Long.parseLong(relateId));
            Page<Object> page = PageHelper.startPage(paging.getCurrentPage(), paging.getPageSize());
            List<ApprovalRecord> approvalRecords =  approvalRecordService.getApprovalList(approvalRecord);
            paging.setTotalPages(page.getPages());
            mav.getModel().put("paging",paging);
            mav.getModel().put("approvalRecords",approvalRecords);
        }
        mav.setViewName("product/approvalPage");
        return mav;
    }

    /**
     * 添加审批记录
     * @param request
     * @param approvalRecord
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/approval")
    public Boolean approval(HttpServletRequest request,ApprovalRecord approvalRecord) {
        Boolean flag = false;
        Integer status = approvalRecord.getStatus();
        if (approvalRecord.getRelateId() != null  && status != null && status >0) {
            try {
                approvalRecord.setAddTime(new Date());
                approvalRecord.setType(IssueType.PRODUCT.getVal());
                approvalRecord.setOperationId(super.getSysUserId(request));
                approvalRecordService.saveApproval(approvalRecord);
                Product product = new Product();
                product.setId(approvalRecord.getRelateId());
                product.setStatus(status);
                productService.updateProduct(product);
                flag = true;
            } catch (Exception e) {
                LogMgr.writeErrorLog(e);
            }
        }
        return flag;
    }
    @ResponseBody
    @RequestMapping(value="/operation")
    public Boolean operation(Product product) {
        Boolean flag = false;
        if (product.getId() != null) {
            try {
                product.setUpdateTime(new Date());
                productService.updateProduct(product);
                flag = true;
            } catch (Exception e) {
                LogMgr.writeErrorLog(e);
            }
        }
        return flag;
    }

    /**
     * 去设置页面
     * @param id
     * @param mav
     * @return
     */
    @RequestMapping(value="/toSetting/{id}")
    public ModelAndView toSetting(@PathVariable("id") long id,ModelAndView mav) {
        Product product = productService.getProductById(id);
        mav.getModel().put("product", product);
        mav.setViewName("product/setting");
        return mav;
    }


}
