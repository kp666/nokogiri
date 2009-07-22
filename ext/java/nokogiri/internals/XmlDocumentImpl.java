package nokogiri.internals;

import nokogiri.XmlDocument;
import nokogiri.XmlNode;
import org.jruby.Ruby;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author sergio
 */
public class XmlDocumentImpl extends XmlNodeImpl{

    protected IRubyObject root;
    protected IRubyObject encoding;

    public XmlDocumentImpl(Ruby ruby, Node node) {
        super(ruby, node);
    }

    @Override
    protected int getNokogiriNodeTypeInternal() { return 10; }

    public IRubyObject encoding(ThreadContext context, XmlDocument current) {
        if(this.encoding == null) {
            if(current.getDocument().getXmlEncoding() == null) {
                this.encoding = context.getRuntime().getNil();
            } else {
                this.encoding = context.getRuntime().newString(current.getDocument().getXmlEncoding());
            }
        }

        return this.encoding;
    }

    public void encoding_set(ThreadContext context, XmlDocument current, IRubyObject encoding) {
        this.encoding = encoding;
    }

    @Override
    public void relink_namespace(ThreadContext context, XmlNode current) {
        XmlDocument cur = (XmlDocument) current;
        ((XmlNode) cur.root(context)).relink_namespace(context);
    }

    public IRubyObject root(ThreadContext context, XmlDocument current) {
        if(this.root == null) {
            this.root = XmlNode.constructNode(context.getRuntime(),
                    current.getDocument().getDocumentElement());
        }
        return root;
    }

    public void root_set(ThreadContext context, XmlDocument current, IRubyObject root) {
        Document document = current.getDocument();
        Node node = XmlNode.getNodeFromXmlNode(context, root);
        document.replaceChild(node, document.getDocumentElement());
        this.root = root;
    }

    @Override
    public void saveContent(ThreadContext context, XmlNode node, SaveContext ctx) {
        XmlDocument cur = (XmlDocument) node;
        Document curDoc = cur.getDocument();

        if(!ctx.noDecl()) {
            
            ctx.append("<?xml version=\"");
            ctx.append(curDoc.getXmlVersion());
            ctx.append("\"");
//            if(!cur.encoding(context).isNil()) {
//                ctx.append(" encoding=");
//                ctx.append(cur.encoding(context).asJavaString());
//            }

            String encoding = ctx.getEncoding();

            if(encoding == null &&
                    !cur.encoding(context).isNil()) {
                encoding = cur.encoding(context).convertToString().asJavaString();
            }

            if(encoding != null) {
                ctx.append(" encoding=\"");
                ctx.append(encoding);
                ctx.append("\"");
            }

            ctx.append(" standalone=\"");
            ctx.append(curDoc.getXmlStandalone() ? "yes" : "no");
            ctx.append("\"?>\n");
        }

        XmlNode root = (XmlNode) cur.root(context);
        root.saveContent(context, ctx);
        ctx.append("\n");
    }

}
